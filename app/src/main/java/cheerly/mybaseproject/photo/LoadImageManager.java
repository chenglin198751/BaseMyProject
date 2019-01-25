package cheerly.mybaseproject.photo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @title: LoadImageManager.java
 * @description: 图片数据库部分操作
 * @company: 美丽说（北京）网络科技有限公司
 * @created：2015年6月12日
 */
public class LoadImageManager {
    // 设置获取图片的字段信息
    public static final String[] STORE_IMAGES = {
            Media.DISPLAY_NAME,    // 显示的名称
            Media.LATITUDE,        // 维度
            Media.LONGITUDE,        // 经度
            Media._ID,            // id
            Media.BUCKET_ID,        // dir id 目录
            Media.BUCKET_DISPLAY_NAME, // dir name 目录名字
            Media.DATA            // 路径
    };

    public static List<PhotoAlbum> getAlbums(Context mContext) {
        if (!isSdcardExist()) {
            return null;
        }
        ArrayList<PhotoAlbum> list = new ArrayList<PhotoAlbum>();
        Uri uri = Media.EXTERNAL_CONTENT_URI.buildUpon()
                .appendQueryParameter("distinct", "true").build();

        String selection = Media.SIZE + ">0) GROUP BY (" + Media.BUCKET_ID + "";
        Cursor cursor = null;
        try {
            cursor = Media.query(mContext.getContentResolver(), uri, STORE_IMAGES, selection, null, Media.DATE_TAKEN + " desc");
        } catch (Exception e) {
            cursor = null;
        }
        if (cursor == null || cursor.isClosed()) {
            return list;
        }

        while (cursor.moveToNext()) {
            PhotoAlbum album = getPhotoAlbum(cursor);
            album.setCount(getPhotoCount(mContext, album.getDirId()));
            list.add(album);
        }

        // 按照名称进行排序
        Collections.sort(list, new AlbumListSortable());

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return list;
    }

    public static ArrayList<PhotoAlbum> getPicsForAlbum(Context c, String id, int count) {
        ArrayList<String> albumIds = new ArrayList<String>();
        albumIds.add(id);
        ArrayList<PhotoAlbum> list = getPicsForAlbumIdsLocal(c, albumIds, count);

        return list;
    }

    /**
     * 是否存在文件夹
     *
     * @param dirId
     * @return
     */
    public static PhotoAlbum existDir(Context context, String dirId) {
        String selection = Media.BUCKET_ID + " = " + dirId
                + " ) group by ( "
                + Media.BUCKET_DISPLAY_NAME;
        Cursor cursor = Media.query(
                context.getContentResolver(),
                Media.EXTERNAL_CONTENT_URI, LoadImageManager.STORE_IMAGES,
                selection, null, Media.DATE_MODIFIED
                        + " DESC");
        if (cursor != null && cursor.moveToNext()) {
            PhotoAlbum photoAlbum = new PhotoAlbum();
            long id = cursor.getLong(cursor
                    .getColumnIndex(Media._ID));
            photoAlbum.setPhotoID(id);
            photoAlbum.setName(cursor.getString(cursor
                    .getColumnIndex(Media.BUCKET_DISPLAY_NAME)));
            photoAlbum.setPath(cursor.getString(cursor
                    .getColumnIndex(Media.DATA)));
            photoAlbum.setDirId(cursor.getLong(cursor
                    .getColumnIndex(Media.BUCKET_ID)));

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            return photoAlbum;
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return null;
    }

    /**
     * 通过所有相册列表，获取默认的选中的一个相册(一般只是在第一次时会使用)
     *
     * @param mPhotoList
     * @return
     */
    public static String getDefaultDirID(List<PhotoAlbum> mPhotoList) {
        String result = "";

        if (mPhotoList == null || mPhotoList.size() <= 0) {
            result = "-1";
            return result;
        }

        for (PhotoAlbum mPhotoAlbum : mPhotoList) {
            if (mPhotoAlbum.getPath() != null) {
                String albumDir = mPhotoAlbum.getPath().toLowerCase();
                String defaultDir = LoadImageManager.getSaveDefaultDir().toLowerCase();
                //修复在Camera下有新的文件夹导致取默认相册出错的bug chenglin 2016-04-27
                if (albumDir.startsWith(defaultDir)) {
                    String subStr = albumDir.substring(defaultDir.length() + 1, albumDir.length());
                    if (!subStr.contains(File.separator)) {
                        result = mPhotoAlbum.getDirId() + "";
                        break;
                    }
                }
            }
        }

        if (TextUtils.isEmpty(result) && mPhotoList != null && mPhotoList.size() > 0) {
            result = mPhotoList.get(0).getDirId() + "";
        }

        if (TextUtils.isEmpty(result)) {
            result = "-1";
        }

        return result;
    }

    private static ArrayList<PhotoAlbum> getPicsForAlbumIdsLocal(Context mContext,
                                                                 List<String> albumidList, int total) {
        ArrayList<PhotoAlbum> picList = new ArrayList<PhotoAlbum>();
        if (albumidList == null || albumidList.size() == 0)
            return picList;
        int size = albumidList.size();
        StringBuilder sb = new StringBuilder(Media.BUCKET_ID + "=\""
                + albumidList.get(0) + "\"");
        for (int i = 1; i < size; i++) {
            sb.append(" or " + Media.BUCKET_ID + "=\"" + albumidList.get(i)
                    + "\"");
        }
        String selection = sb.toString();
        Uri uri = Media.EXTERNAL_CONTENT_URI;
        Cursor c = null;
        try {
            c = Media.query(mContext.getContentResolver(), uri, STORE_IMAGES,
                    selection, null, Media.DATE_TAKEN + " DESC");
            if (c == null || c.isClosed())
                return picList;

            int count = 0;
            while (c.moveToNext()) {
                PhotoAlbum info = getPhotoAlbum(c);
                picList.add(info);
                count++;
                if (total > 0 && count >= total) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return picList;
    }

    private static PhotoAlbum getPhotoAlbum(Cursor cursor) {
        PhotoAlbum photoAlbum = new PhotoAlbum();
        long id = cursor.getLong(cursor
                .getColumnIndex(Media._ID));
        photoAlbum.setPhotoID(id);
        photoAlbum.setName(cursor.getString(cursor
                .getColumnIndex(Media.BUCKET_DISPLAY_NAME)));
        photoAlbum.setPath(cursor.getString(cursor
                .getColumnIndex(Media.DATA)));
        photoAlbum.setDirId(cursor.getLong(cursor
                .getColumnIndex(Media.BUCKET_ID)));
        return photoAlbum;
    }

    private static boolean isSdcardExist() {
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取文件夹下图片个数
     *
     * @param dirId
     * @return
     */
    private static int getPhotoCount(Context mContext, long dirId) {
        String where = Media.BUCKET_ID + " =  " + dirId;
        Cursor cursor = Media
                .query(mContext.getContentResolver(),
                        Media.EXTERNAL_CONTENT_URI, null,
                        where, null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }

    @SuppressLint("NewApi")
    private static String getSaveDefaultDir() {
        File file = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        String defaultPath = "";
        if (file.exists()) {
            defaultPath = file.getAbsolutePath() + "/Camera";
        } else {
            defaultPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/DCIM/Camera";
        }
        return defaultPath;
    }
}
