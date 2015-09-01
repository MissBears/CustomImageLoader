package com.imageloader.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangzhiguo on 15/9/1.
 */
public class ImageLoader {
    private static Context mContext;
    /**
     * 三级缓存
     * 数据结构
     * 一级缓存：强引用缓存(内存)，当内存不足的时候不会去回收对象，宁可抛出内存溢出异常
     *          设定缓存20张图片（使用最新的图片）
     * 二级缓存：软引用缓存(内存)，当内存不足的时候会去回收对象
     *          超过20张的图片
     * 三级缓存：本地缓存(硬盘)
     *          离线缓存
     */
    //一级缓存的容量
    private static final int MAX_CAPACITY = 20;
    //key:图片地址，value:图片
    //true 基于访问排序（LRU 近期最少使用算法），false 基于插入排序
    //需要加线程同步锁
    private static LinkedHashMap<String,Bitmap> firstCacheMap = new LinkedHashMap<String,Bitmap>(MAX_CAPACITY,0.75f,true){
        //根据返回值，移除map中最老的值
        @Override
        protected boolean removeEldestEntry(Entry<String, Bitmap> eldest) {

            if(this.size() > MAX_CAPACITY) {
                //加入二级缓存
                secondCacheMap.put(eldest.getKey(),new SoftReference<Bitmap>(eldest.getValue()));
                //加入本地缓存
                diskCache(eldest.getKey(),eldest.getValue());
                //移除一级缓存
                return true;
            }
            return super.removeEldestEntry(eldest);
        }
    };
    //二级缓存
    //线程安全的
    private static ConcurrentHashMap<String,SoftReference<Bitmap>> secondCacheMap = new ConcurrentHashMap<String,SoftReference<Bitmap>>();

    //三级缓存：本地缓存（硬盘）
    //离线缓存
    //写入内部存储


    private DefaultImage mDefaultImage = new DefaultImage();

    private static ImageLoader mInstance;

    private ImageLoader() {};

    private ImageLoader(Context context) {
        mContext = context;
    }

    public static ImageLoader getInstancee(Context context) {
        if(mInstance == null) {
            mInstance = new ImageLoader(context);
        }
        return mInstance;
    }
    /**
     *
     * @param key 图片地址
     * @param imageView 图片控件
     */
    public void loadImage(String key,ImageView imageView) {
        //读取缓存
        Bitmap bitmap = getFromCache(key);
        if(bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            //访问网络
            //设置空白图片
            imageView.setImageDrawable(mDefaultImage);
            //执行异步任务
            AsynImageLoadTask task = new AsynImageLoadTask(imageView);
            task.execute(key);
        }
    }

    private Bitmap getFromCache(String url) {
        //从一级缓存读取
        synchronized (firstCacheMap) {
            Bitmap bitmap = firstCacheMap.get(url);
            //保持图片是最新的
            if(bitmap != null) {
                firstCacheMap.remove(bitmap);
                firstCacheMap.put(url, bitmap);
                return bitmap;
            }
        }
        //从二级缓存读取
        SoftReference<Bitmap> softBitmap = secondCacheMap.get(url);
        if(softBitmap != null) {
            Bitmap bitmap = softBitmap.get();
            if(bitmap != null) {
                //添加到一级缓存
                firstCacheMap.put(url, bitmap);
                return bitmap;
            }
        } else {
            //软引用已经被回收了
            secondCacheMap.remove(url);
        }
        //从本地缓存读取
        Bitmap localBitmap = getFromLocal(url);
        if(localBitmap != null) {
            //添加到一级缓存
            firstCacheMap.put(url, localBitmap);
            return localBitmap;
        }
        return null;
    }

    /**
     * 从本地缓存中读取
     * @param key
     * @return
     */
    private Bitmap getFromLocal(String key) {
        String fileName = MD5Utils.decode(key);
        if(fileName == null) {
            return null;
        }
        String path = mContext.getCacheDir().getAbsolutePath() + File.separator + fileName;
        FileInputStream is = null;
        try {
            is = new FileInputStream(path);
            return BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(is != null) {
                    is.close();
                }
            } catch (IOException e) {

            }
        }
        return null;
    }

    class  AsynImageLoadTask extends AsyncTask<String,Void,Bitmap> {
        private String key;
        private ImageView mImageView;
        public AsynImageLoadTask(ImageView imageView) {
            super();
            mImageView = imageView;
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            key = params[0];
            return download(key);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //添加到一级缓存
            addFirstCache(key,bitmap);
            //显示图片
            mImageView.setImageBitmap(bitmap);
        }
    }

    private Bitmap download(String url) {
        InputStream is = null;
        try {
            is = HttpUtils.download(url);
            return BitmapFactory.decodeStream(is);
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 添加到一级缓存
     * @param url
     * @param result
     */
    private void addFirstCache(String url,Bitmap result) {
        if(result != null) {
            synchronized (firstCacheMap) {
                firstCacheMap.put(url,result);
            }
        }
    }

    /**
     * 本地缓存
     * @param key 图片的路径（图片的路径会被当作图片的名称保存到硬盘上）
     * @param value
     */
    protected static void diskCache(String key, Bitmap value) {
        //路径（本地文件标示符）
        //http://192.168.1.124:8080/image_web/images/232434Dfdsjfdls.png
        //消息摘要算法(抗修改性)
        //Message Diagest Version 5(MD5)
        String fileName = MD5Utils.decode(key);
        String path = mContext.getCacheDir().getAbsolutePath() + File.separator +fileName;
        //JPG
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(path);
            value.compress(Bitmap.CompressFormat.JPEG,100,os);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 默认图片
     */
    class DefaultImage extends ColorDrawable {
        public DefaultImage() {
            super(Color.GRAY);
        }
    }

}
