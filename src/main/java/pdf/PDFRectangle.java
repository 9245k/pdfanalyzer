package pdf;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.util.Matrix;

public class PDFRectangle {
	boolean mIsFill;
	public Rectangle2D mRectangle2D;
	public PDColorSpace mStrokeColorSpace;
	public PDColorSpace mFillColorSpace;
	public PDColor mStrokeColor;
	public PDColor mFillColor;
	public double mConstantAlpha;
	public Rectangle mRectClip;
	public Matrix mMatrix;
	PDFRectangle(){
		mIsFill = false;
		mRectangle2D = null;
		mStrokeColorSpace = null;
		mFillColorSpace = null;
		mStrokeColor = null;
		mFillColor = null;
		mConstantAlpha = 0;
		mRectClip = null;
		mMatrix = null;
	};

}

