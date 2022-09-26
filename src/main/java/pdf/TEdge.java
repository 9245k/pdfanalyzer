package pdf;


public class TEdge {
	int mID;
	int mSX;
	int mEX;
	TColor mColor;

	TEdge(int sx, int ex, int id, TColor c){
		mSX = sx;
		mEX = ex;
		mID = id;
		mColor = c;
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
	public void setID(int id){
		mID = id;
	}
	public int getID(){
		return mID;
	}
	public boolean equals(TEdge e) {
		if(mID == e.getID() && mSX == e.getSX() && mEX == e.getEX()){
			return true;
		}
		return false;
	}
	public boolean equalsX(TEdge e) {
		TColor c = e.getColor();
		if(mSX == e.getSX() && mEX == e.getEX() &&
				mColor.getR() == c.getR() &&
				mColor.getG() == c.getG() &&
				mColor.getB() == c.getB()){
			return true;
		}
		return false;
	}
	public boolean checkNext(TEdge e){
		TColor c = e.getColor();
		if((mEX + 1) == e.getSX() &&
				mColor.getR() == c.getR() &&
				mColor.getG() == c.getG() &&
				mColor.getB() == c.getB()){
			return true;
		}
		return false;
	}
	public void setColor(TColor c){
		mColor = c;
	}
	public TColor getColor(){
		return mColor;
	}
	public String printString(){
		String msg = "";
		msg = "(" + mID + ":" + mSX + "," + mEX + ")";
		return msg;
	}


}
