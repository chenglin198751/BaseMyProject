package photo;

import android.text.TextUtils;

import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * @title: AlbumListSortable.java
 * @description: 根据相册名称排序规则类
 * @company: 美丽说（北京）网络科技有限公司
 * @author: tinglongyu
 * @version: 6.5.0
 * @created：2015年6月16日
 */
public class AlbumListSortable implements Comparator<PhotoAlbum> {
    private String TAG = "AlbumListSortable";

    @Override
    public int compare(PhotoAlbum lhs, PhotoAlbum rhs) {
        String name1 = lhs.getName();
        String name2 = rhs.getName();
        int digital_1 = isStartDigital(name1);
        int digital_2 = isStartDigital(name2);
        if (digital_1 * digital_2 < 0) {
//			Debug.debug(
//					TAG,
//					"digital: "
//							+ String.format("name1:%s,name2:%s", name1, name2));
            return digital_1;
        }

        int english_1 = isStartEnglish(name1);
        int english_2 = isStartEnglish(name2);
        if (english_1 * english_2 < 0) {
//			Debug.debug(
//					TAG,
//					"english: "
//							+ String.format("name1:%s,name2:%s", name1, name2));
            return english_1 * (-1);
        }
        int chinese_1 = isStartChinese(name1);
        int chinese_2 = isStartChinese(name2);
        if (chinese_1 * chinese_2 < 0) {
//			Debug.debug(
//					TAG,
//					"chinese: "
//							+ String.format("name1:%s,name2:%s", name1, name2));
            if (english_1 == 1) {
                return -1;
            } else if (english_2 == 1) {
                return 1;
            } else {
                if (chinese_1 == 1) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
        for (int l1 = name1.length(), l2 = name2.length(), i = 0; i < Math.min(
                l1, l2); i++) {
            char b1 = name1.charAt(i), b2 = name2.charAt(i), ib1 = Character
                    .toLowerCase(b1), ib2 = Character.toLowerCase(b2);
            if (ib1 == ib2) {
                if (b1 > b2) {
                    return (1);
                } else if (b1 < b2) {
                    return (-1);
                }
            } else if (ib1 > ib2) {
                return (1);
            } else if (ib1 < ib2) {
                return (-1);
            }
        }
        return name1.compareTo(name2);
    }

    /**
     * 数字开头
     *
     * @param name
     * @return
     */
    public int isStartDigital(String name) {
        if (TextUtils.isEmpty(name)) {
            return -1;
        }
        String s = name.substring(0, 1);
        boolean result = Pattern.matches("^[0-9]{1,}$", s);
        return result ? 1 : -1;
    }

    /**
     * 英文开头
     *
     * @param name
     * @return
     */
    public int isStartEnglish(String name) {
        if (TextUtils.isEmpty(name)) {
            return -1;
        }
        String s = name.substring(0, 1);
        boolean result = Pattern.matches("[A-Za-z]", s);
        return result ? 1 : -1;
    }

    /**
     * 中文开头
     *
     * @param name
     * @return
     */
    public int isStartChinese(String name) {
        if (TextUtils.isEmpty(name)) {
            return -1;
        }
        String s = name.substring(0, 1);
        boolean result = Pattern.matches(
                "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D]{1,}$", s);
        return result ? 1 : -1;
    }
}
