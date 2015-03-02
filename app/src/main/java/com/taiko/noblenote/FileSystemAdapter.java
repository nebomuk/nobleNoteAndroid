/**
 * 
 */
package com.taiko.noblenote;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author Taiko-G780
 *
 */
public class FileSystemAdapter extends ArrayAdapter<File> {

	public static final int FILE_EXISTS = 1;
	public static final int NAME_EMPTY = 2;
	public static final int FILE_NOT_EXISTS = 3;
	public static final int FAILED = 4;
	
	private LayoutInflater mInflater;
	private int mFieldId;
	private int mResource;
	private File mRootFile;
	
	
	public FileSystemAdapter(Context context, int textViewResourceId, File path, FileFilter filter) 
	{
		super(context, textViewResourceId, listFiles(path,filter));
		sort();
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mFieldId = 0;
		mResource = textViewResourceId;	
		mRootFile = path;
	}
	
	/**
	 *  creates a writable list of the contents of the directory
	 */
	public static ArrayList<File> listFiles(File dir, FileFilter filter)
	{
		//List<File> fileList = Arrays.asList(); // returns read only list, causes unsupported operation exceptions in adapter
		ArrayList<File> fileList = new ArrayList<File>();
		for(File f : dir.listFiles(filter))
			fileList.add(f);
		return fileList;
	}
	
	// copied from ArrayAdapter
	public View getView(int position, View convertView, ViewGroup parent) {
		        return createViewFromResource(position, convertView, parent, mResource);
		    }
	
	// copied from ArrayAdapter
    private View createViewFromResource(int position, View convertView, ViewGroup parent,
            int resource) {
        View view;
        TextView text;

        if (convertView == null) {
            view = mInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
           } else {
                //  Otherwise, find the TextView field within the layout
                text = (TextView) view.findViewById(mFieldId);
            }
        } catch (ClassCastException e) {
            Log.e("FileSystemAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "FileSystemAdapter requires the resource ID to be a TextView", e);
        }

        File item = getItem(position);
        text.setText(item.getName());

        return view;
    }

    /**
     * sort alphabetically
     */
	public void sort() {
		this.sort(new Comparator<File>()
		{
			@Override
			public int compare(File lhs, File rhs) {
				return Collator.getInstance().compare(lhs.getName(), rhs.getName());
			}
		});		
	}
	
	public boolean rename(File oldFile, File newFile)
	{
		if(!oldFile.renameTo(newFile))
			return false;
			
		remove(oldFile);
		add(newFile);
		sort();	
		
		return true;
	}
	
	public int rename(File file, String newName)
	{
		if(newName == null || newName.equals(""))
			return NAME_EMPTY;
			
		File newFile = new File(file.getParent() + File.separator + newName);
		
		if(newFile.exists())
			return FILE_EXISTS;
		
		if(!rename(file, newFile))
			return FAILED;
				
		return 0;
	}
	
	public int createFile(File file)
	{
		if(file.getName().equals(""))
			return NAME_EMPTY;
		
		if(file.exists())
			return FILE_EXISTS;
		
		try {
			if(!file.createNewFile())
				return FAILED;
		} catch (IOException e) {
				return FAILED;
		}
		
		add(file);
		
		return 0;		
	}
	
	public int mkdir(File dir)
	{
		if(dir.getName().equals(""))
			return NAME_EMPTY;
		
		if(dir.exists())
			return FILE_EXISTS;
		
		if(!dir.mkdir())
			return FAILED;
		
		add(dir);
		
		return 0;
	}
	
	public int removeFile(File file)
	{
		if(file.getName().equals(""))
			return NAME_EMPTY;
		
		if(!file.exists())
			return FILE_NOT_EXISTS;
		
		if(!file.delete())
			return FAILED;
		
		remove(file);
		
		return 0;		
	}

	public File getRootDir() {
		return mRootFile;
	}
}
