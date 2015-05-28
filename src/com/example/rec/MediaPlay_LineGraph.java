package com.example.rec;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;

public class MediaPlay_LineGraph {
	private GraphicalView view;

	private TimeSeries dataset = new TimeSeries("dB측정");
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	public XYSeriesRenderer renderer = new XYSeriesRenderer();
	public XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

	public MediaPlay_LineGraph() {
		// Add single dataset to multiple dataset
		mDataset.addSeries(dataset);

		// 그래프의 선 스타일 설정
		renderer.setColor(Color.WHITE);
		renderer.setPointStyle(PointStyle.CIRCLE);
		renderer.setLineWidth(10);
		renderer.setFillPoints(true);

		//X,Y축 항목이름과 글자 크기
		mRenderer.setXTitle("경과시간");
		mRenderer.setYTitle("데시벨");
		mRenderer.setAxisTitleTextSize(20);
		//수치값 글자 크기와 X,Y축 최소,최대 값
		mRenderer.setLabelsTextSize(10);
		mRenderer.setYAxisMin(-50);
		mRenderer.setYAxisMax(150);
		//XY축의 표시간격
		mRenderer.setYLabels(10);
		mRenderer.setXLabels(10);
		//그래프 배경 바꾸기
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.BLACK);
		//그래프의 배경에 격자 보여주기
		mRenderer.setShowGrid(true);

		// Add single renderer to multiple renderer
		mRenderer.addSeriesRenderer(renderer);
	}

	public GraphicalView getView(Context context) {
		view = ChartFactory.getLineChartView(context, mDataset, mRenderer);
		return view;
	}

	public void addNewPoints(MediaPlay_Point p) {
		dataset.add(p.getX(), p.getY());
	}
}