Theme {
	classvar <>font;
	classvar <>grey;
	classvar <>background;
	classvar <>horizontalUnit; 
	classvar <>verticalUnit;
	
	*initClass {
		font = Font("Helvetica", 10);
		grey = Color.grey(0.1, 0.5);
		background = Color(0.8588, 0.8588, 0.8588);
		horizontalUnit = 50;
		verticalUnit = 40;
	}
}