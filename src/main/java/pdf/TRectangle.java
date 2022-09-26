package pdf;


public class TRectangle {

	int mID;
	int mSX;
	int mEX;
	int mSY;
	int mEY;
	TColor mColor;


	public void setID(int id){
		mID = id;
	}
	public int getID(){
		return mID;
	}
	public void setSX(int sx){
		mSX = sx;
	}
	public int getSX(){
		return mSX;
	}
	public void setEX(int ex){
		mEX = ex;
	}
	public int getEX(){
		return mEX;
	}
	public void setSY(int sy){
		mSY = sy;
	}
	public int getSY(){
		return mSY;
	}
	public void setEY(int ey){
		mEY = ey;
	}
	public int getEY(){
		return mEY;
	}

	public int getW(){
		return mEX - mSX + 1;
	}

	public int getH(){
		return mEY - mSY + 1;
	}

	public void setColor(TColor c){
		mColor = c;
	}

	public TColor getColor(){
		return mColor;
	}

	public boolean checkNextHolizontal(TRectangle r){

		TColor c = r.getColor();
		if(
				(getSY() == r.getSY()) &&
				(getEY() == r.getEY()) &&
				(getEX() == (r.getSX() - 1)) &&
				mColor.getR() == c.getR() &&
				mColor.getG() == c.getG() &&
				mColor.getB() == c.getB()){
			return true;
		}
		return false;
	}

	public boolean checkSame(TRectangle r){

		TColor c = r.getColor();
		if(
				(getSX() == r.getSX()) &&
				(getSY() == r.getSY()) &&
				(getEX() == r.getEX()) &&
				(getEY() == r.getEY()) &&
				mColor.getR() == c.getR() &&
				mColor.getG() == c.getG() &&
				mColor.getB() == c.getB()){
			return true;
		}
		return false;
	}

	public String printString(){
		String msg = "";
		msg = "(" + mID + ":" + mSX + "," + mSY + "," + (mEX - mSX + 1) + "," +  (mEY - mSY + 1) + ")";
		return msg;
	}


}
