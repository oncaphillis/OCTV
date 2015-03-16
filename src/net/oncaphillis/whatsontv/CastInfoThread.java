package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.people.Person;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CastInfoThread extends Thread {
	
	private Activity _activity = null;
	private TableLayout _table = null;
	private int _maxcol = 1;
    
    private List< ? extends Person > _credits[] = null;
    private String[] _titles = null;
    
	class InfoNode {
		public TableRow row = null;
		public ArrayList<ImageView>   img = new ArrayList<ImageView>();
		public ArrayList<ProgressBar> pb  = new ArrayList<ProgressBar>();
	};
	
	CastInfoThread(Activity activity,TableLayout table,int maxcol,
			List<? extends Person> [] credits,String[] titles) {
		_activity = activity;
		_table    = table;
		_maxcol   = maxcol; 

		_credits = credits; 
		_titles = titles;
	}

	@Override
	public void run() {
		_activity.runOnUiThread(new Runnable() {
	    	@Override
		    public void run() {		

			    final LayoutInflater vi = LayoutInflater.from(_table.getContext());
					
	    		for(int person_group=0;person_group<_credits.length;person_group++) {
			        if(_credits[person_group]==null)
			        	continue;
			        
					Iterator<? extends Person> it = _credits[person_group].iterator();

					int cc=0;
					TableRow  tr=null;
					if(it.hasNext()) {
						tr = new TableRow(_table.getContext());
				        LinearLayout header = (LinearLayout) vi.inflate(R.layout.series_table_header,null);
				        TextView txt = (TextView)header.findViewById(R.id.series_table_header_text);
				        tr.addView(header);
				        
				        TableRow.LayoutParams params = (TableRow.LayoutParams)header.getLayoutParams();
				        params.span = _maxcol;
				        header.setLayoutParams(params);
				        
				        if(_titles != null && person_group < _titles.length) {
				        	txt.setText(Integer.toString(_credits[person_group].size())+" "
				        			+_titles[person_group]);
				        } else {
				        	txt.setText(Integer.toString(_credits[person_group].size()));
				        }
				        	_table.addView(tr);
				        
				        List<InfoNode> trl = new ArrayList();
				        
						while(it.hasNext()) {
							if(cc>=_maxcol)
								cc=0;
							
							if(cc==0) {
								InfoNode n = new InfoNode();
								tr = new TableRow(_table.getContext());
								tr.setOrientation(LinearLayout.HORIZONTAL);
								_table.addView(tr);
								n.row = tr;
								trl.add(n);
								tr.setVisibility(View.GONE);
							}

							Person p = it.next();
							
							String nn = p.getName(); 
							String pp = p.getProfilePath();
							String rr = null;
							
							if(p instanceof PersonCast) {
								rr = ((PersonCast)p).getCharacter();
							} else if(p instanceof PersonCrew) {
								rr = ((PersonCrew)p).getJob();
							} else  {
								rr = null;
							} 
							
							View v = (View)vi.inflate(R.layout.series_info_grid_entry,null);
				        	
					        TextView       tt0 = (TextView)     v.findViewById(R.id.creator_name);
					        TextView       tt1 = (TextView)     v.findViewById(R.id.person_role);
						    ImageView       ii = (ImageView)    v.findViewById(R.id.creator_image);
						    ProgressBar     pb = (ProgressBar)  v.findViewById(R.id.creator_image_progress);
						    										    
						    ii.setTag(pp);
	
						    tt0.setText(nn == null ? "" : nn);
						    tt1.setText(rr == null ? "" : rr);
						    
						    trl.get(trl.size()-1).img.add(ii);
						    trl.get(trl.size()-1).pb.add(pb);
						    
						    tr.addView(v);
						    cc++;
						}
						
			    		final List<InfoNode> ftrl = trl;
			    		header.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								LinearLayout ll = (LinearLayout) v;
								Iterator<InfoNode> i = ftrl.iterator();
								boolean f = false;
								while(i.hasNext()) {
									InfoNode n = i.next();
									if(n.row.getVisibility()!=View.VISIBLE) {
										n.row.setVisibility(View.VISIBLE);
										f = true;
										if(n.img!=null) {
											for(int j=0;j<n.img.size();j++) {
												new BitmapDownloaderTask(n.img.get(j), _activity, n.pb.get(j), null,null).execute();
											}
											n.img = null;
											n.pb = null;
										}
									}
									else {
										n.row.setVisibility(View.GONE);
									}
								}
								ImageView iv = (ImageView)ll.findViewById(R.id.series_table_header_expand);
								iv.setImageDrawable(_activity.getResources().getDrawable(f ? R.drawable.down : R.drawable.right));
							}
			    		});
					}
	    		}
	    	}
		});
	}				
}

