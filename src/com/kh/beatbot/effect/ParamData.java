package com.kh.beatbot.effect;

public class ParamData {
	public float addValue, scaleValue, logScaleValue;
	public boolean hz, beatSyncable, logScale;
	public String unitString, name;
	
	public ParamData(String name, boolean logScale, boolean beatSyncable,
			float addValue, float scaleValue, float logScaleValue, String unitString) {
		this.name = name;
		this.beatSyncable = beatSyncable;
		this.logScale = logScale;
		this.logScaleValue = logScaleValue;
		this.addValue = addValue;
		this.scaleValue = scaleValue;
		this.unitString = unitString;
		this.hz = unitString.equalsIgnoreCase("hz");
	}

	public ParamData(String name, boolean logScale, boolean beatSyncable,
			String unitString) {
		this(name, logScale, beatSyncable, 0, 1, 8, unitString);
	}
}
