Theme {
	classvar <>font;
	classvar <>grey;
	classvar <>darkGrey;
	classvar <>background;
	classvar <>horizontalUnit; 
	classvar <>verticalUnit;
	
	*initClass {
		font = Font("Helvetica", 10);
		grey = Color.grey(0.1, 0.5);
		darkGrey = Color.grey(0.05, 0.5);
		background = Color(0.8588, 0.8588, 0.8588);
		horizontalUnit = 100;
		verticalUnit = 40;
	}
}
