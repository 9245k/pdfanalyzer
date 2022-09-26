package pdf;

import java.awt.Color;

public class TColor {

	int mRGBColor;

	public TColor(){
		mRGBColor = 0x00ffffff;
	}

	public TColor(int c){
		mRGBColor = c;
	}

	int getR(){
		int red = (mRGBColor >> 16) & 0x000000ff;
		return red;
	}
	int getG(){
		int red = (mRGBColor >> 8) & 0x000000ff;
		return red;
	}
	int getB(){
		int red = mRGBColor & 0x000000ff;
		return red;
	}

	Color getAWTColor(){
		Color c = new Color(getR(), getG(), getB());
		return c;
	}

	boolean equalsValue(TColor c){
		if(c.getR() == getR() && c.getG() == getG() && c.getB() == getB()){
			return true;
		}
		return false;
	}
}
