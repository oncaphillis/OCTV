package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.people.Person;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import android.app.Activity;
import android.graphics.Bitmap;
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
    
	private static final int CAST    = 0;
    private static final int CREW    = 1;
    private static final int CREATOR = 2;
    
    static final String[] _info_type={"Cast","Crew","Creator"};
    
    private List< ? extends Person > _credits[] = new List[3];
    
	class InfoNode {
		public TableRow row = null;
		public ArrayList<ImageView>   img = new ArrayList();
		public ArrayList<ProgressBar> pb  = new ArrayList();
	};
	
	CastInfoThread(Activity activity,TableLayout table,int maxcol,
			List<? extends Person> cast,List<? extends Person> crew,List<? extends Person> creator) {
		_activity = activity;
		_table = table;
		_maxcol = maxcol; 
		for(int i=0;i<_credits.length;i++) {
			_credits[i]=null;
		}
		_credits[0] = cast; 
		_credits[1] = crew; 
		_credits[2] = creator; 
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
				        
				        txt.setText(_info_type[person_group]);
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

							String nn;
							String rr;
							String pp;
							
							if(person_group==CAST) {
								PersonCast p = (PersonCast)it.next();
								nn = p.getName();
								rr = p.getCharacter();
								pp = p.getProfilePath();
							} else if(person_group==CREW) {
								PersonCrew p = (PersonCrew)it.next();
								nn = p.getName();
								rr = p.getJob();
								pp = p.getProfilePath();
							} else  {
								Person p = (Person)it.next();
								nn = p.getName();
								rr = null;
								pp = p.getProfilePath();
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

