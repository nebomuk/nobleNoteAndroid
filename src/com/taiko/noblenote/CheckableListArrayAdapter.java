/**
 * 
 */
package com.taiko.noblenote;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author Taiko-G780
 * 
 * an ArrayAdapter that uses the .toString method of any supplied generic type object to 
 * set the text of checkable list items
 *
 */
public class CheckableListArrayAdapter<T> extends ArrayAdapter<T> 
{
	private LayoutInflater li;

	/**
	 * Constructor from a list of items
	 */
	public CheckableListArrayAdapter(Context context, List<T> items) {
		super(context, 0, items);
		li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		final T tItem = getItem(position);

		// Re-use the view if possible
		// --
		View v = convertView;
		if (v == null) {
			v = li.inflate(R.layout.checkable_list_item, null);
		}
		
		final TextView idView = (TextView) v.findViewById(R.id.checkableListItemText);
		if (idView != null) {
			idView.setText(tItem.toString());
		}

		return v;
	}
};
