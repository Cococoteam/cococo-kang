package com.example.rec;

import org.achartengine.*;
import org.achartengine.chart.*;
import org.achartengine.model.*;
import org.achartengine.renderer.*;

import android.content.*;
import android.graphics.*;

public class RecPage_LineGraph {

	private GraphicalView view;

	//private TimeSeries dataset = new TimeSeries("dB측정");
	private TimeSeries dataset;
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	public XYSeriesRenderer renderer = new XYSeriesRenderer();
	public XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

	public RecPage_LineGraph(int i) {
		if(i == 1)
			dataset = new TimeSeries("dynamic");
		else
			dataset = new TimeSeries("static");
		
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
		mRenderer.setLabelsTextSize(15);
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

	public void addNewPoints(RecPage_Point p) {
		dataset.add(p.getX(), p.getY());
	}
	
	public void clearAll(){
		dataset.clear();
		view.repaint();
	}
	
	public void removeData(int[][] tmpdB, RecPage_Point p){
		dataset.clear();
		for(int i=0; i<20; i++){
			p.setXY(tmpdB[i][0], tmpdB[i][1]);
			dataset.add(p.getX(), p.getY());
		}
	}
	
	public int getCount(){
		return dataset.getItemCount();
	}
}
