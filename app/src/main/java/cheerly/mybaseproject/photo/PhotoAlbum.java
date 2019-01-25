package cheerly.mybaseproject.photo;

import java.io.Serializable;

/**
 * 
 * @title:
 * @description:
 * @company: 美丽说（北京）网络科技有限公司
 * @author yinxinya
 * @version 1.0
 * @created
 * @changeRecord
 */
public class PhotoAlbum implements Serializable {

	private static final long serialVersionUID = 1L;
	private long photoID;// 图片id
	private String mPath;// 图片路径
	private String name; // 相册名字
	private int count; // 数量
	private long dirId;// 目录ID

	public PhotoAlbum() {
	}

	public PhotoAlbum(long id, String path) {
		photoID = id;
		mPath = path;
	}

	public PhotoAlbum(int id, boolean flag) {
		photoID = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setDirId(long dirId) {
		this.dirId = dirId;
	}

	public long getDirId() {
		return dirId;
	}

	public long getPhotoID() {
		return photoID;
	}

	public void setPhotoID(long photoID) {
		this.photoID = photoID;
	}

	public void setPath(String path) {
		mPath = path;
	}

	public String getPath() {
		return mPath;
	}

}
