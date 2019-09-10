package com.marchnetworks.management.file.service;

import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.common.event.util.Pair;
import com.marchnetworks.management.data.FileStatusResult;
import com.marchnetworks.management.data.FileStorageView;
import com.marchnetworks.management.data.UpdFileInfo;
import com.marchnetworks.management.file.model.FileStorageMBean;
import com.marchnetworks.management.file.model.FileStorageType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public interface FileStorageService
{
	public static final String KEY_NAME = "fileName";
	public static final String KEY_PATH = "filePath";
	public static final String KEY_REQUEST_ID = "requestId";
	public static final String DEFAULT_PATH = "/";

	FileStorageView createFileStorage( String paramString, InputStream paramInputStream ) throws FileStorageException;

	FileStorageView createFileStorage( String paramString, InputStream paramInputStream, Boolean paramBoolean ) throws FileStorageException;

	FileStorageView addFileStorage( String paramString, File paramFile, List<Pair> paramList ) throws FileStorageException;

	boolean isFileStorageExist( String paramString );

	FileStorageView getFileStorage( String paramString ) throws FileStorageException;

	FileStorageView getFileStorageByName( String paramString ) throws FileStorageException;

	void deleteFileStorage( String paramString ) throws FileStorageException;

	File getFile( String paramString ) throws FileNotFoundException, FileStorageException;

	void setFileStorageProperty( FileStorageView paramFileStorageView, String paramString1, String paramString2 );

	String getFileStorageProperty( FileStorageView paramFileStorageView, String paramString );

	List<FileStorageView> getFileStorageList( FileStorageType paramFileStorageType ) throws FileStorageException;

	List<FileStorageView> getFileStorageListByProperty( String paramString1, String paramString2 ) throws FileStorageException;

	FileStorageMBean getFileStorageObject( String paramString );

	FileStorageView[] getFileStorageListAsArray( FileStorageType paramFileStorageType ) throws FileStorageException;

	List<FileStorageView> getFileStorageByProperties( Pair... paramVarArgs );

	FileStorageView getFirstMatchFileStorage( String paramString, DeviceView paramDeviceView );

	FileStatusResult validateFile( String paramString, InputStream paramInputStream, File paramFile );

	boolean fileInUseCheck( String paramString );

	UpdFileInfo getUpdFileInfo( String paramString1, String paramString2 );
}

