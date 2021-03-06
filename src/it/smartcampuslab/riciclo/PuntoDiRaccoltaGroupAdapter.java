package it.smartcampuslab.riciclo;

import it.smartcampuslab.riciclo.data.RifiutiHelper;
import it.smartcampuslab.riciclo.model.DatiTipologiaRaccolta;
import it.smartcampuslab.riciclo.model.PuntoRaccolta;
import it.smartcampuslab.riciclo.utils.LocationHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PuntoDiRaccoltaGroupAdapter extends BaseExpandableListAdapter {
	private Activity activity;
	private int layoutItemId;
	private int layoutGroupId;
	private SparseArray<List<PuntoRaccolta>> points;
	private SparseArray<List<DatiTipologiaRaccolta>> infos;

	public PuntoDiRaccoltaGroupAdapter(Activity activity, int layoutGroupId, int layoutItemId, List<PuntoRaccolta> objects,
			List<DatiTipologiaRaccolta> infos) {
		this.activity = activity;
		this.layoutGroupId = layoutGroupId;
		this.layoutItemId = layoutItemId;
		this.points = new SparseArray<List<PuntoRaccolta>>();
		this.infos = new SparseArray<List<DatiTipologiaRaccolta>>();
		Map<String, Integer> map = new HashMap<String, Integer>();

		if (objects != null && objects.size() > 1) {
			Collections.sort(objects, new LocationHelper.PRDistanceComparator(RifiutiHelper.locationHelper.getLocation()));
		}

		// Collections.sort(objects, new Comparator<PuntoRaccolta>() {
		// public int compare(PuntoRaccolta lhs, PuntoRaccolta rhs) {
		// return
		// lhs.getTipologiaPuntiRaccolta().compareTo(rhs.getTipologiaPuntiRaccolta());
		// }
		// });
		if (infos != null) {
			for (DatiTipologiaRaccolta dtr : infos) {
				Integer key = map.get(dtr.getTipologiaPuntoRaccolta());
				if (key == null) {
					key = map.size();
					map.put(dtr.getTipologiaPuntoRaccolta(), key);
					this.points.append(key, new ArrayList<PuntoRaccolta>());
					this.infos.append(key, new ArrayList<DatiTipologiaRaccolta>());
				}
				this.infos.get(key).add(dtr);
			}
			if (points != null) {
				for (PuntoRaccolta pr : objects) {
					Integer key = map.get(pr.getTipologiaPuntiRaccolta());
					this.points.get(key).add(pr);
				}
			}
		} else {
			for (PuntoRaccolta pr : objects) {
				Integer key = map.get(pr.getTipologiaPuntiRaccolta());
				if (key == null) {
					key = map.size();
					map.put(pr.getTipologiaPuntiRaccolta(), key);
					this.points.append(key, new ArrayList<PuntoRaccolta>());
					this.infos.append(key, new ArrayList<DatiTipologiaRaccolta>());
				}
				points.get(key).add(pr);
			}
		}
	}

	@Override
	public PuntoRaccolta getChild(int groupPosition, int childPosition) {
		return points.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		View row;
		if (convertView == null) {
			row = activity.getLayoutInflater().inflate(layoutItemId, parent, false);
		} else {
			row = convertView;
		}
		TextView title = (TextView) row.findViewById(R.id.puntoraccolta_title);
		TextView dist = (TextView) row.findViewById(R.id.puntoraccolta_distance);
		PuntoRaccolta pr = getChild(groupPosition, childPosition);
		if (pr.location() != null && RifiutiHelper.locationHelper.getLocation() != null) {
			Location l = new Location("");
			l.setLatitude(pr.location()[0]);
			l.setLongitude(pr.location()[1]);
			double distKm = l.distanceTo(RifiutiHelper.locationHelper.getLocation()) / 1000;
			dist.setText(activity.getString(R.string.distance, distKm));
			dist.setVisibility(View.VISIBLE);
		} else {
			dist.setVisibility(View.GONE);
		}
		title.setText(pr.dettaglio());
		return row;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return points.get(groupPosition).size();
	}

	@Override
	public List<PuntoRaccolta> getGroup(int groupPosition) {
		return points.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return points.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = activity.getLayoutInflater().inflate(layoutGroupId, null);
		}
		List<DatiTipologiaRaccolta> groupInfos = infos.get(groupPosition);
		List<PuntoRaccolta> groupPoints = points.get(groupPosition);

		TextView tipoPuntoTv = (TextView) convertView.findViewById(R.id.title_tv);
		TextView tipoRaccoltaTv = (TextView) convertView.findViewById(R.id.tiporaccolta_tv);
		TextView infoTv = (TextView) convertView.findViewById(R.id.info_tv);
		ImageView icon = (ImageView) convertView.findViewById(R.id.tiporaccolta_img);

		String tipoPuntoRaccolta = null;
		String color = null;
		String info = null;
		String tipoRaccolta = null;
		if (groupInfos != null && !groupInfos.isEmpty()) {
			tipoPuntoRaccolta = groupInfos.get(0).getTipologiaPuntoRaccolta();
			tipoPuntoTv.setText(tipoPuntoRaccolta);
			Set<String> tipoRaccoltaSet = new HashSet<String>();
			Set<String> colorSet = new HashSet<String>();
			Set<String> infoSet = new HashSet<String>();
			for (DatiTipologiaRaccolta dtr : groupInfos) {
				tipoRaccoltaSet.add(dtr.getTipologiaRaccolta());
				colorSet.add(dtr.getColore());
				infoSet.add(dtr.getInfo());
			}
			if (infoSet.size() == 1)
				info = infoSet.iterator().next();
			if (colorSet.size() == 1)
				color = colorSet.iterator().next();
			if (tipoRaccoltaSet.size() == 1)
				tipoRaccolta = tipoRaccoltaSet.iterator().next();
			tipoRaccoltaTv.setVisibility(View.VISIBLE);
		} else {
			tipoPuntoRaccolta = groupPoints.get(0).getTipologiaPuntiRaccolta();
			tipoPuntoTv.setText(tipoPuntoRaccolta);
			tipoRaccoltaTv.setVisibility(View.GONE);
		}
		if (info == null) {
			infoTv.setVisibility(View.GONE);
		} else {
			infoTv.setVisibility(View.VISIBLE);
			infoTv.setText(info);
		}
		if (tipoRaccolta == null) {
			tipoRaccoltaTv.setVisibility(View.GONE);
		} else {
			tipoRaccoltaTv.setVisibility(View.VISIBLE);
			tipoRaccoltaTv.setText(tipoRaccolta);
		}
		Drawable res = RifiutiHelper.getTypeColorResource(activity, tipoPuntoRaccolta, color);
		if (res != null) {
			icon.setImageDrawable(res);
		}

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
