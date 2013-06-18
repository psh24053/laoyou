package com.shntec.saf;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;


/**
 * 缓存管理器
 * @author shihao
 *
 */
public class SAFCache {

	/**
	 * 重新实现safcache，safcache存在的目的是为了储存model对象
	 * 以及普通基本类型数据。图片和文件的缓存，将使用imageManager进行管理
	 * safcache所储存的每个缓存都不会超过1M，每个储存的model对象以文
	 * 件的形式储存在cacheDir目录下，文件名为key，文件内容为序列化之后的字
	 * 符串。而基本类型数据，则使用XML文件进行储存，对应的XML文件名为Config.BaseXMLName
	 * 在safcache中能够对这两种缓存进行CRUD操作。
	 */
	
	
	private static SAFCache safcache = new SAFCache();

	private SAFCache() {}
	private Context c;
	private Map<String, File> fileCache = new HashMap<String, File>();
	private SharedPreferences p = null;
	private File cacheDir = null;
	private File filesDir = null;
	private File sdCardFilesDir = null;
	private boolean sdCard = false;
	private static String BaseXMLName = "SAF_BASE_CACHE";
	private long minRomSize = 1024 * 1024; // 内存空间少于minRomSize时，不进行IO操作
	private long minSDCardSize = 1024 * 1024 * 20; // SD卡空间少于minSDCardSize时，不进行IO操作
	public static final String TAG = "SAFCache";
	private boolean init = false;
	private Map<String, Object> objCache = new HashMap<String, Object>();
	
	public static SAFCache getInstance(){
		if(safcache.init){
			return safcache;
		}
		return null;
	}
	
	public static SAFCache getInstance(Context context) {
		safcache.c = context;
		safcache.p = context.getSharedPreferences(BaseXMLName, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		
		safcache.cacheDir = new File(context.getFilesDir(),"/cache/");
		safcache.filesDir = new File(context.getFilesDir(),"/files/");
		safcache.sdCardFilesDir = new File(Environment.getExternalStorageDirectory(),"/SAF/files");
		
		// 判断SD卡是否存在
		safcache.sdCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		
		//如果文件夹不存在则创建
		if(!safcache.cacheDir.exists()){
			safcache.cacheDir.mkdir();
		}
		if(!safcache.filesDir.exists()){
			safcache.filesDir.mkdir();
		}
		if(safcache.sdCard){
			if(!safcache.sdCardFilesDir.exists()){
				safcache.sdCardFilesDir.mkdirs();
			}
		}
		safcache.init = true;
		
		return safcache;
	}

	/**
	 * 获取缓存占用总大小，单位是字节
	 * @return
	 */
	public long getCacheTotalSize(){
		long cacheTotal = 0;
		//得到对象缓存文件夹的文件数组
		File[] cacheFiles = cacheDir.listFiles();
		for(File file : cacheFiles){
			cacheTotal += file.length();
		}
		//得到文件缓存ROM文件夹的文件数组
		File[] fileFiles = filesDir.listFiles();
		for(File file : fileFiles){
			cacheTotal += file.length();
		}
		//如果SD卡存在
		if(sdCard){
			//获取SD卡文件夹的文件数组
			File[] sdFiles = sdCardFilesDir.listFiles();
			for(File file : sdFiles){
				cacheTotal += file.length();
			}
		}
		
		return cacheTotal;
	}
	
	/**
	 * 获取内容空间剩余大小，单位为字节
	 * @return
	 */
	public long getRootDirAvailableSize(){
		File root = Environment.getRootDirectory();
		StatFs sf = new StatFs(root.getPath());
		
		long blockSize = sf.getBlockSize(); 
        long availCount = sf.getAvailableBlocks(); 

		return blockSize * availCount;
	}
	/**
	 * 获取内存空间总大小，单位为字节
	 * @return
	 */
	public long getRootDirTotalSize(){
		File root = Environment.getRootDirectory();
		StatFs sf = new StatFs(root.getPath());
		
		long blockSize = sf.getBlockSize(); 
        long blockCount = sf.getBlockCount(); 
        
		return blockSize * blockCount;
	}
	/**
	 * 获取SD卡空间总大小，单位为字节
	 * @return
	 */
	public long getSDCardTotalSize(){
		File sdcard = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(sdcard.getPath());
		
		long blockSize = sf.getBlockSize(); 
        long blockCount = sf.getBlockCount(); 
        
		return blockSize * blockCount;
	}
	/**
	 * 获取SD卡空间剩余大小，单位为字节
	 * @return
	 */
	public long getSDCardAvailableSize(){
		File sdcard = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(sdcard.getPath());
		
		long blockSize = sf.getBlockSize(); 
        long availCount = sf.getAvailableBlocks(); 

		return blockSize * availCount;
	}
	/**
	 * 清除内存中的索引
	 */
	public void ClearMemoryIndex(){
		fileCache.clear();
	}
	/**
	 * 清理所有对象缓存
	 */
	public void ClearObjectCache(){
		
		//得到文件数组
		File[] files = cacheDir.listFiles();
		for(int i = 0 ; i < files.length ; i ++){
			File itemFile = files[i];
			//删除文件
			if(itemFile.exists() && itemFile.isFile() && itemFile.canWrite()){
				itemFile.delete();
			}
		}
	}
	/**
	 * 清理所有对象缓存，传入例外对象名数组
	 * @param filenames
	 */
	public void ClearObjectCacheExcept(String[] filenames){
		
		File[] files = cacheDir.listFiles();
		for(int i = 0 ; i < files.length ; i ++){
			File itemFile = files[i];
			// 例外判断
			boolean clear = true;
			
			for(String name : filenames){
				if(itemFile.getName().equals(name)){
					clear = false;
					break;
				}
			}
			
			
			//删除文件
			if(clear && itemFile.exists() && itemFile.isFile() && itemFile.canWrite()){
				itemFile.delete();
			}
		}
	}
	/**
	 * 清理所有基本类型缓存
	 */
	public void ClearBaseCache(){
		Editor e = p.edit();
		e.clear();
		e.commit();
	}
	/**
	 * 清理所有文件缓存，内存和SD卡一块儿清
	 */
	public void ClearFilesCache(){
		
		//得到文件数组，得到内存文件缓存目录
		File[] files = filesDir.listFiles();
		for(int i = 0 ; i < files.length ; i ++){
			File itemFile = files[i];
			//删除文件
			if(itemFile.exists() && itemFile.isFile() && itemFile.canWrite()){
				itemFile.delete();
			}
		}
		// 如果sd卡存在，则清理sd卡目录
		if(sdCard){
			
			//得到文件数组，得到内存文件缓存目录
			File[] sd = sdCardFilesDir.listFiles();
			for(int i = 0 ; i < sd.length ; i ++){
				File itemFile = sd[i];
				//删除文件
				if(itemFile.exists() && itemFile.isFile() && itemFile.canWrite()){
					itemFile.delete();
				}
			}
			
		}
		
		
	}
	/**
	 * 清楚对象缓存，根据uid
	 * @param uid
	 */
	public void ClearObjectCacheForUid(int uid){
		
		//得到文件数组
		File[] files = cacheDir.listFiles();
		for(int i = 0 ; i < files.length ; i ++){
			File itemFile = files[i];
			String fileName = itemFile.getName();
			//删除文件
			if(itemFile.exists() && itemFile.isFile() && itemFile.canWrite()){
				
				if(fileName.contains("_"+uid)){
					itemFile.delete();
				}
				
				
			}
		}
		
	}
	
	
	/**
	 * 清理所有文件缓存，内存和SD卡一块儿清
	 * 传入例外文件名数组
	 */
	public void ClearFilesCacheExcept(String[] filenames){
		
		//得到文件数组，得到内存文件缓存目录
		File[] files = filesDir.listFiles();
		for(int i = 0 ; i < files.length ; i ++){
			File itemFile = files[i];
			// 例外判断
			boolean clear = true;
			
			for(String name : filenames){
				if(itemFile.getName().equals(name)){
					clear = false;
					break;
				}
			}
			
			
			//删除文件
			if(clear && itemFile.exists() && itemFile.isFile() && itemFile.canWrite()){
				itemFile.delete();
			}
		}
		// 如果sd卡存在，则清理sd卡目录
		if(sdCard){
			
			//得到文件数组，得到内存文件缓存目录
			File[] sd = sdCardFilesDir.listFiles();
			for(int i = 0 ; i < sd.length ; i ++){
				File itemFile = sd[i];
				
				// 例外判断
				boolean clear = true;
				
				for(String name : filenames){
					if(itemFile.getName().equals(name)){
						clear = false;
						break;
					}
				}
				//删除文件
				if(clear && itemFile.exists() && itemFile.isFile() && itemFile.canWrite()){
					itemFile.delete();
				}
			}
			
		}
		
	}
	/**
	 * 判断文件缓存中是否存在key
	 * @param key
	 * @return
	 */
	public boolean hasFilesCache(String key){
		// 首先判断sd卡是否存在
		if(sdCard){
			// 得到文件数组
			File[] files = sdCardFilesDir.listFiles();
			
			//遍历文件数组
			for(int i = 0 ; i < files.length ; i ++){
				File itemFile = files[i];
				//判断文件是否存在，文件是否是一个文件，文件是否能读，是否能写，并且文件的名字与key相同
				//则代表对象缓存中存在这个key
				if(itemFile.exists() && itemFile.isFile() && itemFile.canRead() && itemFile.canWrite() && itemFile.getName().equals(key)){
					//将这个itemFile加入到内存中，为近期使用做准备
					Log.i(TAG, "safCache.hasFilesCache -> " +key+" -> true by sdcard");
					fileCache.put(key, itemFile);
					return true;
				}
			}

		}
		// 得到文件数组
		File[] files = filesDir.listFiles();
		
		//遍历文件数组
		for(int i = 0 ; i < files.length ; i ++){
			File itemFile = files[i];
			//判断文件是否存在，文件是否是一个文件，文件是否能读，是否能写，并且文件的名字与key相同
			//则代表对象缓存中存在这个key
			if(itemFile.exists() && itemFile.isFile() && itemFile.canRead() && itemFile.canWrite() && itemFile.getName().equals(key)){
				//将这个itemFile加入到内存中，为近期使用做准备
				Log.i(TAG, "safCache.hasFilesCache -> " +key+" -> true by rom");
				fileCache.put(key, itemFile);
				return true;
			}
		}
		
		
		
		return false;
	}
	/**
	 * 判断对象缓存中是否存在key
	 * @param key
	 * @return
	 */
	public boolean hasObjectCache(String key){

		Log.i(TAG, "safCache.hasObjectCache -> " +key);
		
		//得到文件数组
		File[] files = cacheDir.listFiles();
		//遍历文件数组
		for(int i = 0 ; i < files.length ; i ++){
			File itemFile = files[i];
			//判断文件是否存在，文件是否是一个文件，文件是否能读，是否能写，并且文件的名字与key相同
			//则代表对象缓存中存在这个key
			if(itemFile.exists() && itemFile.isFile() && itemFile.canRead() && itemFile.canWrite() && itemFile.getName().equals(key)){
				//将这个itemFile加入到内存中，为近期使用做准备
				Log.i(TAG, "safCache.hasObjectCache -> " +key+" -> true");
				fileCache.put(key, itemFile);
				return true;
			}
		}
		
		Log.i(TAG, "safCache.hasObjectCache -> " +key+" -> false");
		return false;
	}
	
	/**
	 * 判断普通缓存中是否存在key
	 * @param key
	 * @return
	 */
	public boolean hasBaseCache(String key){
		return p.contains(key);
	}
	/**
	 * 保存文件缓存，根据key
	 * @param key
	 * @param is
	 * @return
	 * @throws IOException 
	 */
	public synchronized boolean SaveFilesCache(String key, InputStream is) throws IOException{
		
		// 如果sd卡存在，则优先使用sd卡
		if(sdCard){
			// 如果sd卡可用空间少于minSDCardSize，则不进行IO操作
			long availableSize = getSDCardAvailableSize();
			long romsize = getRootDirAvailableSize();
			if(availableSize < minSDCardSize){
				//SD卡空间不足，再来判断手机内存空间是否充足
				if(romsize < minSDCardSize / 5){
					// 如果手机rom空间也不足，那么不进行IO操作
					return false;
				}else{
					// 手机rom可以使用
					
					//创建输出文件对象
					File outFile = new File(filesDir, key);

					Log.i(TAG, "safCache.SaveFilesCache -> " +key+" -> by rom");
					// 创建缓冲输入流
					BufferedInputStream bis = new BufferedInputStream(is);
					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					
					byte[] readByte = new byte[1024];
					int readCount = -1;
					// 读取输入流
					while((readCount = bis.read(readByte, 0, 1024)) != -1){
						baos.write(readByte, 0, readCount);
					}
					
					bis.close();
					// 创建文件输出流
					FileOutputStream fos = new FileOutputStream(outFile);
					fos.write(baos.toByteArray());
					fos.flush();
					fos.close();
					baos.close();
					
					// 将这个文件加入到FileCache中
					Log.i(TAG, "safCache.SaveFilesCache -> " +key+" -> true -> by sdcard");
					fileCache.put(key, outFile);
					return true;
					
				}
					
			}else{
				// SD卡可以使用
				
				//创建输出文件对象
				File outFile = new File(sdCardFilesDir, key);

				Log.i(TAG, "safCache.SaveFilesCache -> " +key+" -> by sdcard");
				// 创建缓冲输入流
				BufferedInputStream bis = new BufferedInputStream(is);
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				byte[] readByte = new byte[1024];
				int readCount = -1;
				// 读取输入流
				while((readCount = bis.read(readByte, 0, 1024)) != -1){
					baos.write(readByte, 0, readCount);
				}
				bis.close();
				// 创建文件输出流
				FileOutputStream fos = new FileOutputStream(outFile);
				fos.write(baos.toByteArray());
				fos.flush();
				fos.close();
				baos.close();
				
				// 将这个文件加入到FileCache中
				Log.i(TAG, "safCache.SaveFilesCache -> " +key+" -> true -> by sdcard");
				fileCache.put(key, outFile);
				return true;
				
			}
			
			
			
			
		}else{
			//如果rom可用空间少于minRomSize，则不进行IO操作
			long availableSize = getRootDirAvailableSize();
			if(availableSize < minRomSize){
				return false;
			}
			
			//创建输出文件对象
			File outFile = new File(filesDir, key);

			Log.i(TAG, "safCache.SaveFilesCache -> " +key+" -> by rom");
			// 创建缓冲输入流
			BufferedInputStream bis = new BufferedInputStream(is);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			byte[] readByte = new byte[1024];
			int readCount = -1;
			// 读取输入流
			while((readCount = bis.read(readByte, 0, 1024)) != -1){
				baos.write(readByte, 0, readCount);
			}
			
			bis.close();
			// 创建文件输出流
			FileOutputStream fos = new FileOutputStream(outFile);
			fos.write(baos.toByteArray());
			fos.flush();
			fos.close();
			baos.close();
			// 将这个文件加入到FileCache中
			Log.i(TAG, "safCache.SaveFilesCache -> " +key+" -> true -> by rom");
			fileCache.put(key, outFile);
			return true;
			
			
		}
	
	}
	/**
	 * 保存一个缓存，传入一个序列化对象
	 * @param key
	 * @param object
	 * @throws IOException 
	 */
	public synchronized boolean SaveObjectCache(String key, Serializable object) throws IOException{

		//如果rom可用空间少于minRomSize，则不进行IO操作
		long availableSize = getRootDirAvailableSize();
		if(availableSize < minRomSize){
			return false;
		}
		
		//创建输出文件对象
		File outFile = new File(cacheDir, key);

		Log.i(TAG, "safCache.SaveObjectCache -> " +key+" -> "+object.toString());
		// 创建文件输出流
		FileOutputStream fos = new FileOutputStream(outFile);
		// 创建对象输出流，传入文件输出流
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		// 写出object，并关闭流
		oos.writeObject(object);
		oos.flush();
		oos.close();
		fos.close();
		
		// 将这个文件加入到FileCache中
		Log.i(TAG, "safCache.SaveObjectCache -> " +key+" -> "+object.toString()+" -> true");
		fileCache.put(key, outFile);
		return true;
		
		
	}
	/**
	 * 读取一个文件缓存，根据key
	 * @param key
	 * @return
	 * @throws FileNotFoundException 
	 */
	public InputStream readFilesCache(String key) throws FileNotFoundException{
		File readFile = null;
		//如果fileCache中存在这个key，则使用这个file
		//否则将从cacheDir中遍历读取
		Log.i(TAG, "safCache.readFilesCache -> " +key);
		if(fileCache.containsKey(key)){
			readFile = fileCache.get(key);
		}else{
			//直接调用hasFilesCache方法来判断文件是否存在，如果不存在则返回false
			if(!hasFilesCache(key)){
				Log.i(TAG, "safCache.readFilesCache -> " +key+" -> null");
				return null;
			}else{
				readFile = fileCache.get(key);
			}
			
		}
		
		// 创建文件输入流
		FileInputStream fis = new FileInputStream(readFile);
		
		Log.i(TAG, "safCache.readFilesCache -> " +key +" -> true -> by "+(sdCard?"sdcard":"rom"));
		return fis;
	}
	/**
	 * 根据key，获取指定文件缓存的大小
	 * @param key
	 * @return
	 * @throws IOException 
	 */
	public long readFileSize(String key) throws IOException{
		File readFile = null;
		//如果fileCache中存在这个key，则使用这个file
		//否则将从cacheDir中遍历读取
		Log.i(TAG, "safCache.readFilesCache -> " +key);
		if(fileCache.containsKey(key)){
			readFile = fileCache.get(key);
		}else{
			//直接调用hasFilesCache方法来判断文件是否存在，如果不存在则返回false
			if(!hasFilesCache(key)){
				Log.i(TAG, "safCache.readFilesCache -> " +key+" -> null");
				return 0;
			}else{
				readFile = fileCache.get(key);
			}
			
		}
		
		// 创建文件输入流
		FileInputStream fis = new FileInputStream(readFile);
		long totalSize = fis.available();
		Log.i(TAG, "safCache.readFileSize -> " +key +" -> true -> by "+totalSize);
		fis.close();
		return totalSize;
	}
	
	/**
	 * 读取一个文件缓存，根据key
	 * @param key
	 * @return
	 * @throws FileNotFoundException 
	 */
	public File FileFilesCache(String key) throws FileNotFoundException{
		File readFile = null;
		//如果fileCache中存在这个key，则使用这个file
		//否则将从cacheDir中遍历读取
		Log.i(TAG, "safCache.readFilesCache -> " +key);
		if(fileCache.containsKey(key)){
			readFile = fileCache.get(key);
		}else{
			//直接调用hasFilesCache方法来判断文件是否存在，如果不存在则返回false
			if(!hasFilesCache(key)){
				Log.i(TAG, "safCache.readFilesCache -> " +key+" -> null");
				return null;
			}else{
				readFile = fileCache.get(key);
			}
			
		}
		
		
		Log.i(TAG, "safCache.readFilesCache -> " +key +" -> true -> by "+(sdCard?"sdcard":"rom"));
		return readFile;
	}

	/**
	 * 根据key，读取一个对象缓存，如果没有读到，则返回null
	 * @param key
	 * @return
	 * @throws IOException 
	 * @throws StreamCorruptedException 
	 * @throws ClassNotFoundException 
	 */
	public Object readObjectCache(String key) throws StreamCorruptedException, IOException, ClassNotFoundException{
		File readFile = null;
		//如果fileCache中存在这个key，则使用这个file
		//否则将从cacheDir中遍历读取
		Log.i(TAG, "safCache.readObjectCache -> " +key);
		if(fileCache.containsKey(key)){
			readFile = fileCache.get(key);
		}else{
			//直接调用hasObjectCache方法来判断文件是否存在，如果不存在则返回false
			if(!hasObjectCache(key)){
				Log.i(TAG, "safCache.readObjectCache -> " +key+" -> null");
				return null;
			}else{
				readFile = fileCache.get(key);
			}
			
		}
		
		if(!readFile.exists()){
			return null;
		}
		
		// 创建文件输入流
		FileInputStream fis = new FileInputStream(readFile);
		// 创建对象输入流
		ObjectInputStream ois = new ObjectInputStream(fis);
		// 返回反序列化后的对象，并关闭IO流
		Object o = ois.readObject();
		ois.close();
		fis.close();
		
		
		Log.i(TAG, "safCache.readObjectCache -> " +key+" -> "+o.toString());
		return o;
		
		
	}
	/**
	 * 根据key，删除一个对象缓存
	 * @param key
	 * @return
	 */
	public boolean removeObjectCache(String key){
		
		if(hasObjectCache(key)){
			File removeFile = fileCache.get(key);
			Log.i(TAG, "safCache.removeObjectCache -> " +key);
			// 如果removeFile存在并且是个文件并且可以写，则删除它，并返回删除结果
			if(removeFile.exists() && removeFile.isFile() && removeFile.canWrite()){
				return removeFile.delete();
			}
		}else{
			return false;
		}
		
		return false;
	}

	/**
	 * 删除一个文件缓存，根据key
	 * @param key
	 * @return
	 */
	public boolean removeFilesCache(String key){
		if(hasFilesCache(key)){
			File removeFile = fileCache.get(key);
			Log.i(TAG, "safCache.removeFilesCache -> " +key);
			// 如果removeFile存在并且是个文件并且可以写，则删除它，并返回删除结果
			if(removeFile.exists() && removeFile.isFile() && removeFile.canWrite()){
				return removeFile.delete();
			}
		}else{
			return false;
		}
		
		return false;
	}

	/**
	 * 根据Key，来获取对象缓存的File对象
	 * @param key
	 * @return
	 */
	public File getObjectCacheFile(String key){
		if(hasObjectCache(key)){
			return fileCache.get(key);
		}
		
		return null;
	}
	/**
	 * 根据Key，来获取文件缓存的File对象
	 * @param key
	 * @return
	 */
	public File getFilesCacheFile(String key){
		if(hasFilesCache(key)){
			return fileCache.get(key);
		}
		return null;
	}
	
	/**
	 * 获取SharedPreferences对象，用于基本类型的缓存操作
	 * @return
	 */
	public SharedPreferences getP() {
		return p;
	}
	public void setP(SharedPreferences p) {
		this.p = p;
	}
	/**
	 * 获取文件的md5
	 * @param by
	 * @return
	 */
	public String getFileMD5(byte[] by) {
		if (by == null) {
			return null;
		}
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.update(by);
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
	}

	public boolean isSdCard() {
		return sdCard;
	}

	public void setSdCard(boolean sdCard) {
		this.sdCard = sdCard;
	}

	public File getCacheDir() {
		return cacheDir;
	}

	public void setCacheDir(File cacheDir) {
		this.cacheDir = cacheDir;
	}

	public File getFilesDir() {
		return filesDir;
	}

	public void setFilesDir(File filesDir) {
		this.filesDir = filesDir;
	}

	public File getSdCardFilesDir() {
		return sdCardFilesDir;
	}

	public void setSdCardFilesDir(File sdCardFilesDir) {
		this.sdCardFilesDir = sdCardFilesDir;
	}
	
	public static boolean isInit() {
		return safcache.init;
	}

	public Map<String, Object> getObjCache() {
		return objCache;
	}

	public void setObjCache(Map<String, Object> objCache) {
		this.objCache = objCache;
	}

	
	
}
