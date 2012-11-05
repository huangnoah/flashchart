/* Flashchart.java

	Purpose:

	Description:

	History:
		Nov 26, 2009 11:58:42 AM , Created by joy

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under LGPL Version 2.1 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zul;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.zkoss.json.JSONObject;
import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.sys.ContentRenderer;
import org.zkoss.zul.event.ChartDataEvent;
import org.zkoss.zul.event.ChartDataListener;
/**
 * The generic flash chart component. Developers set proper chart type, data model,
 * and the src attribute to draw proper chart. The model and type must match to each other;
 * or the result is unpredictable.
 *
 * <table>
 *   <tr><th>type</th><th>model</th></tr>
 *   <tr><td>pie</td><td>{@link PieModel}</td></tr>
 *   <tr><td>bar</td><td>{@link CategoryModel}</td></tr>
 *   <tr><td>line</td><td>{@link CategoryModel}</td></tr>
 *   <tr><td>column</td><td>{@link CategoryModel}</td></tr>
 *   <tr><td>stackbar</td><td>{@link CategoryModel}</td></tr>
 *   <tr><td>stackcolumn</td><td>{@link XYModel}</td></tr>
 * </table>
 *
 * <p>Default {@link #getWidth}: 400px
 * <p>Default {@link #getHeight}: 200px
 *
 * @author Joy Lo
 * @since 5.0.0
 */
public class Flashchart extends Flash {

	private static final long serialVersionUID = 20091126115842L;
	/**
	 * Declares attributes.
	 */
	private String _type = "pie";
	private String _chartStyle;
	private ChartModel _model;
	private ChartDataListener _dataListener;
	private LinkedList<JSONObject> _seriesList;
	private String _yAxis = "Series 1";
	private String _xAxis = "Series 2";
	
	/**
	 * Sets default values.
	 */
	public Flashchart() {
		setWidth("400px");
		setHeight("200px");
	}
	private class DefaultChartDataListener implements ChartDataListener, Serializable {
		private static final long serialVersionUID = 20091125153002L;

		public void onChange(ChartDataEvent event) {
			refresh();
		}
	}
	private void refresh() {
		smartUpdate("refresh", getJSONResponse(transferToJSONObject(getModel())));
	}
	
	/**
	 * RenderProperties method will bind the attributes with FlashChart.js.
	 */
	protected void renderProperties(ContentRenderer renderer) throws IOException {
		super.renderProperties(renderer);
		render(renderer, "type", _type.split(":")[0]);
		if (_chartStyle != null)
			render(renderer, "chartStyle", _chartStyle);
		render(renderer, "jsonModel", getJSONResponse(transferToJSONObject(getModel())));
		if (!_type.equals("pie"))
			render(renderer, "jsonSeries", getJSONResponse(_seriesList));
	}
	
	/**
	 * Sets the type of chart.
	 * <p>Default: "pie"
	 * <p>Allowed Types: pie, line, bar, column, stackbar, stackcolumn
	 */
	public void setType(String type) {
		if (!isValid(type))
			throw new UiException("Type must be one of the following: " + 
					"pie, line, bar, column, stackbar, stackcolumn");
		if (!Objects.equals(_type, type)) {
			_type = type;
			invalidate(); // Always redraw
		}
	}
	
	/**
	 * Returns the type of chart
	 */
	public String getType() {
		return _type;
	}
	
	/**
	 * Sets the model of chart. The chart will be redrawn if setting an different model.
	 * <p>Only implement models which matched the allowed types
	 * @param model
	 * @see #setType(String)
	 */
	public void setModel(ChartModel model) {
		if (_model != model) {
			if (_model != null)
				_model.removeChartDataListener(_dataListener);
			
			_model = model;
			
			if (_dataListener == null) {
				_dataListener = new DefaultChartDataListener();
				_model.addChartDataListener(_dataListener);
			}
			invalidate(); // Always redraw
		}
	}
	
	/**
	 * Returns the model of chart.
	 */
	public ChartModel getModel() {
		return _model;
	}
	
	/**
	 * Sets X-Axis name of chart. If doesn't set this attribute, then default will shows Series 2.
	 * <p>Default: Series 2
	 * <p>Only used for StackColumnChart and it only works when the chart initial.
	 */
	public void setXaxis(String xAxis) {
		if(xAxis != null){
			_xAxis = xAxis;
			invalidate(); // Always redraw
		}
	}
	
	/**
	 * Returns the name of X-Axis
	 */
	public String getXaxis() {
		return _xAxis;
	}
	
	/**
	 * Sets Y-Axis name of chart. If doesn't set this attribute, then default will shows Series 1.
	 * <p>Default: Series 1
	 * <p>Only used for StackColumnChart and it only works when the chart initial.
	 */
	public void setYaxis(String yAxis) {
		if (yAxis != null) {
			_yAxis = yAxis;
			invalidate(); // Always redraw
		}
	}
	
	/**
	 * Returns the name of Y-Axis.
	 */
	public String getYaxis() {
		return _yAxis;
	}
	
	/**
	 * Sets the content style of flashchart.
	 * <p>Default format: "Category-Attribute=Value", ex."legend-display=right"
	 */
	public void setChartStyle(String chartStyle) {
		if (!Objects.equals(_chartStyle, chartStyle)) {
			_chartStyle = chartStyle;
			invalidate(); // Always redraw
		}
	}
	
	/**
	 * Returns the content style.
	 */
	public String getChartStyle() {
		return _chartStyle;
	}
	
	private List<JSONObject> transferToJSONObject(ChartModel model) {
		LinkedList<JSONObject> list = new LinkedList<JSONObject>();
		
		if (model == null || _type == null)
			return list;
		
		if ("pie".equals(_type)) {
			PieModel tempModel = (PieModel) model;
			for (int i = 0; i < tempModel.getCategories().size(); i++) {
				Comparable category = tempModel.getCategory(i);
				JSONObject json = new JSONObject();
				json.put("categoryField", category);
				json.put("dataField", tempModel.getValue(category));
				list.add(json);
			}
		
		} else {
			_seriesList = new LinkedList<JSONObject>();
			CategoryModel tempModel = (CategoryModel) model;
			int seriesLength = tempModel.getSeries().size();
			String fieldKey = isHorizontal(_type) ? "xField" : "yField";
			for (int i = 0; i < seriesLength; i++) {
				Comparable series = tempModel.getSeries(i);
				JSONObject json = new JSONObject();
				json.put(fieldKey, escape(series));
				json.put("displayName", series);
				_seriesList.add(json);
			}
			for (int i = 0; i < tempModel.getCategories().size(); i++) {
				Comparable category = tempModel.getCategory(i);
				JSONObject jData = new JSONObject();
				jData.put("values", category);
				for (int j = 0; j < seriesLength; j++) {
					Comparable series = tempModel.getSeries(j);
					jData.put(escape(series), tempModel.getValue(series, category));
				}
				list.add(jData);
			}
		}
		return list;
	}
	
	
	
	// helper //
	private static String escape(Object series) {
		return "'" + series + "'";
	}
	
	private static boolean isHorizontal(String type) {
		return "bar".equals(type) || "stackbar".equals(type);
	}
	
	private static final List _VALID_TYPES = Arrays.asList(new Object[] {
			"pie", "line", "bar", "column", "stackbar", "stackcolumn"
	});
	
	private static boolean isValid(String type) {
		return _VALID_TYPES.contains(type);
	}
	
	private static String getJSONResponse(List list) {
		// list may be null.
		if (list == null)
			return "";
		
	    final StringBuffer sb = new StringBuffer().append('[');
	    for (Iterator it = list.iterator(); it.hasNext();) {
	    	String s = String.valueOf(it.next());
            sb.append(s).append(',');
	    }
	    sb.deleteCharAt(sb.length() - 1);
	    sb.append(']');
	    return sb.toString().replaceAll("\\\\", "");
	}
	
}