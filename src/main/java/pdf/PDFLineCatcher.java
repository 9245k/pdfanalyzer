package pdf;

import java.awt.geom.FlatteningPathIterator;

// https://www.codetd.com/en/article/13873339

import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;


public class PDFLineCatcher extends PDFGraphicsStreamEngine
{

	private GeneralPath mLinePath = null;
	private ArrayList<PDFRectangle> mRectList= null;
	private int mClipWindingRule = -1;
	private static String mHeaderRecord = "Text|Page|x|y|width|height|space|font";


	boolean mDebug = false;
	//boolean mDebug = true;


	public PDFLineCatcher(PDPage page)
	{
		super(page);
		mLinePath = new GeneralPath();
		mRectList= new ArrayList<PDFRectangle>();
		mClipWindingRule = -1;
	}


	/*
	public static void main(String[] args) throws IOException
	{
		if( args.length != 2 )
		{
			usage();
		}
		else
		{
			PDDocument document = null;
			FileOutputStream fop = null;
			File file;
			Writer osw = null;
			int numPages;
			double page_height;
			try
			{
				document = PDDocument.load( new File(args[0]));
				numPages = document.getNumberOfPages();
				file = new File(args[1]);
				fop = new FileOutputStream(file);

				// if file doesnt exists, then create it
				if (!file.exists()) {
					file.createNewFile();
				}

				osw = new OutputStreamWriter(fop, "UTF8");
				osw.write(mHeaderRecord + System.lineSeparator());
				//System.out.println("Line Processing numPages:" + numPages);
				for (int n = 0; n < numPages; n++) {
					//System.out.println("Line Processing page:" + n);
					mRectList = new ArrayList<PDFRectangle>();
					PDPage page = document.getPage(n);
					page_height = page.getCropBox().getUpperRightY();
					PDFLineCatcher lineCatcher = new PDFLineCatcher(page);
					lineCatcher.processPage(page);

					try{
						for(PDFRectangle rect:mRectList) {

							String pageNum = Integer.toString(n + 1);
							String x = Double.toString(rect.mRectangle2D.getX());
							String y = Double.toString(page_height - rect.mRectangle2D.getY()) ;
							String w = Double.toString(rect.mRectangle2D.getWidth());
							String h = Double.toString(rect.mRectangle2D.getHeight());
							writeToFile(pageNum, x, y, w, h, osw);

						}
						mRectList = null;
						page = null;
						lineCatcher = null;
					}
					catch(IOException io){
						throw new IOException("Failed to Parse document for line processing. Incorrect document format. Page:" + n);
					}
				};

			}
			catch(IOException io){
				throw new IOException("Failed to Parse document for line processing. Incorrect document format.");
			}
			finally
			{
				if ( osw != null ){
					osw.close();
				}
				if( document != null )
				{
					document.close();
				}
			}
		}
	}
	*/

	private static void writeToFile(String pageNum, String x, String y, String w, String h, Writer osw) throws IOException {
		String c = "^" + "|" +
				pageNum + "|" +
				x + "|" +
				y + "|" +
				w + "|" +
				h + "|" +
				"999" + "|" +
				"marker-only";
		osw.write(c + System.lineSeparator());
	}

	public List<PDFRectangle> getRects(){
		return mRectList;

	}

	@Override
	public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException
	{
		//System.out.println("appendRectangle"+p0.toString()+","+p1.toString()+","+p2.toString()+","+p3.toString());
		// to ensure that the path is created in the right direction, we have to create
		// it by combining single lines instead of creating a simple rectangle
		mLinePath.moveTo((float) p0.getX(), (float) p0.getY());
		mLinePath.lineTo((float) p1.getX(), (float) p1.getY());
		mLinePath.lineTo((float) p2.getX(), (float) p2.getY());
		mLinePath.lineTo((float) p3.getX(), (float) p3.getY());

		// close the subpath instead of adding the last line so that a possible set line
		// cap style isn't taken into account at the "beginning" of the rectangle
		mLinePath.closePath();
	}

	@Override
	public void drawImage(PDImage pdi) throws IOException
	{
	}

	@Override
	public void clip(int windingRule) throws IOException
	{
		//System.out.println("clip:windingRule="+mClipWindingRule);
		// the clipping path will not be updated until the succeeding painting operator is called
		mClipWindingRule = windingRule;

	}

	@Override
	public void moveTo(float x, float y) throws IOException
	{
		//System.out.println("moveTo:"+x+","+y);
		mLinePath.moveTo(x, y);
	}

	@Override
	public void lineTo(float x, float y) throws IOException
	{
		//System.out.println("lineTo:"+x+","+y);
		mLinePath.lineTo(x, y);
	}

	@Override
	public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException
	{
		//System.out.println("curveTo:"+x1+","+y1+","+x2+","+y1);
		mLinePath.curveTo(x1, y1, x2, y2, x3, y3);
	}

	@Override
	public Point2D getCurrentPoint() throws IOException
	{
		//System.out.println("getCurrentPoint:");
		return mLinePath.getCurrentPoint();
	}

	@Override
	public void closePath() throws IOException
	{
		//System.out.println("closePath:");
		mLinePath.closePath();
	}

	@Override
	public void endPath() throws IOException
	{
		//System.out.println("endPath:windingRule="+mClipWindingRule);
		if (mClipWindingRule != -1)
		{
			mLinePath.setWindingRule(mClipWindingRule);
			getGraphicsState().intersectClippingPath(mLinePath);
			mClipWindingRule = -1;
		}
		mLinePath.reset();

	}

	@Override
	public void strokePath() throws IOException
	{
		System.out.println("strokePath:*rectList*");

		PDFRectangle info = new PDFRectangle();
		info.mRectangle2D = mLinePath.getBounds2D();
		mRectList.add(info);
		//System.out.println("fillPath:windingRule="+mClipWindingRule);
		printPath(mLinePath, mDebug);
		printRect(info.mRectangle2D, mDebug);
		mLinePath.reset();

		//properties
		PDGraphicsState GS = getGraphicsState();
		info.mIsFill = true;
        info.mFillColorSpace = GS.getNonStrokingColorSpace();
        info.mFillColor = GS.getNonStrokingColor();
        info.mMatrix = GS.getCurrentTransformationMatrix();
        if(GS.getCurrentClippingPath().isRectangular()){
        	info.mRectClip = GS.getCurrentClippingPath().getBounds();
    		printRect(info.mRectClip, mDebug);

        }



/*
		{
			PathIterator i = mLinePath.getPathIterator(null);
			float[] coords = new float[6];
			System.out.print("P");
			float px = -1;
			float py = -1;
			float x = -1;
			float y = -1;
			while (!i.isDone()) {
				int cs = i.currentSegment(coords);
				switch(cs) {
				case PathIterator.SEG_CUBICTO:
				case PathIterator.SEG_QUADTO:
					System.out.print(" C");
					//System.out.print("[("+coords[0]+","+coords[1]+")("+coords[2]+","+coords[3]+")("+coords[4]+","+coords[5]+")]");
					//addPath(px, py, coords[0], coords[1]);
					x = coords[4];
					y = coords[5];
					break;
				case PathIterator.SEG_LINETO:
					System.out.print(" L");
					x = coords[0];
					y = coords[1];
					addPath(px, py, x, y);
					break;
				case PathIterator.SEG_MOVETO:
					System.out.print(" M");
					x = coords[0];
					y = coords[1];
					break;
				case PathIterator.SEG_CLOSE:
					System.out.print(" CL");
					//addPath(px, py, x, y);
					break;
				default:
					System.out.print(" XX");
					break;
				}
				px = x;
				py = y;
				i.next();
			}
			System.out.println("");
		}

		//System.out.println("");
		//printPath2(mLinePath, true);
		//printRect(info.mRectangle2D, true);
		mLinePath.reset();
		// TODO stroke

		 */
	}

	private void addPath(float px, float py, float x, float y){
		GeneralPath path_ = new GeneralPath();
		path_.moveTo(px, py);
		path_.lineTo(x,y);
		PDFRectangle info_ = new PDFRectangle();
		info_.mRectangle2D = path_.getBounds2D();
		mRectList.add(info_);
		System.out.print(" [R]");

		PDGraphicsState GS = getGraphicsState();
		info_.mIsFill = true;
        info_.mFillColorSpace = GS.getStrokingColorSpace();
        info_.mFillColor = GS.getStrokingColor();
        info_.mMatrix = GS.getCurrentTransformationMatrix();
        if(GS.getCurrentClippingPath().isRectangular()){
        	info_.mRectClip = GS.getCurrentClippingPath().getBounds();
    		printRect(info_.mRectClip, mDebug);

        }

	}

	@Override
	public void fillPath(int windingRule) throws IOException
	{
		//System.out.println("fillPath:*rectList*");
		PDFRectangle info = new PDFRectangle();
		info.mRectangle2D = mLinePath.getBounds2D();
		mRectList.add(info);
		//System.out.println("fillPath:windingRule="+mClipWindingRule);
		printPath(mLinePath, mDebug);
		printRect(info.mRectangle2D, mDebug);
		mLinePath.reset();

		//properties
		PDGraphicsState GS = getGraphicsState();
		info.mIsFill = true;
        info.mFillColorSpace = GS.getNonStrokingColorSpace();
        info.mFillColor = GS.getNonStrokingColor();
        info.mMatrix = GS.getCurrentTransformationMatrix();
        if(GS.getCurrentClippingPath().isRectangular()){
        	info.mRectClip = GS.getCurrentClippingPath().getBounds();
    		printRect(info.mRectClip, mDebug);

        }

	}

	@Override
	public void fillAndStrokePath(int windingRule) throws IOException
	{
		System.out.println("fillAndStrokePath:*rectList*");
		PDFRectangle info = new PDFRectangle();
		info.mRectangle2D = mLinePath.getBounds2D();
		mRectList.add(info);
		//System.out.println("fillAndStrokePath:windingRule="+mClipWindingRule);
		printPath(mLinePath, mDebug);
		printRect(info.mRectangle2D, mDebug);
		mLinePath.reset();

		//properties
		PDGraphicsState GS = getGraphicsState();
		info.mIsFill = true;
        info.mFillColorSpace = GS.getNonStrokingColorSpace();
        info.mFillColor = GS.getNonStrokingColor();
        info.mMatrix = GS.getCurrentTransformationMatrix();
        if(GS.getCurrentClippingPath().isRectangular()){
        	info.mRectClip = GS.getCurrentClippingPath().getBounds();
    		printRect(info.mRectClip, mDebug);
        }

		// TODO stroke
	}

	@Override
	public void shadingFill(COSName cosn) throws IOException
	{
		//System.out.println("shadingFill:");
	}

	/**
	 * This will print the usage for this document.
	 */
	private static void usage()
	{
		System.err.println( "Usage: java " + PDFLineCatcher.class.getName() + " <input-pdf>"  + " <output-file>");
	}


	private void printPath(GeneralPath gp, boolean debug) {
		if(debug) {
			PathIterator i = new FlatteningPathIterator(gp.getPathIterator(null), 1d);
			float[] coords = new float[6];
			System.out.print("P");
			int count = 0;
			while (!i.isDone()) {
				count ++;
				i.currentSegment(coords);
				String X = String.format("%.3f", coords[0]);
				String Y = String.format("%.3f", coords[1]);
				System.out.print("["+X+","+Y+"]");
				i.next();
			}
			System.out.println("");
		}

	}

	private void printPath2(GeneralPath gp, boolean debug) {
		if(debug) {
			PathIterator i = gp.getPathIterator(null);
			float[] coords = new float[6];
			System.out.print("P");
			int count = 0;
			while (!i.isDone()) {
				count ++;
				int cs = i.currentSegment(coords);
				if(cs == PathIterator.SEG_CUBICTO || cs == PathIterator.SEG_QUADTO) {
					String X = String.format("%.3f", coords[4]);
					String Y = String.format("%.3f", coords[5]);
					System.out.print("["+X+","+Y+"]");
					System.out.println("");
				}
				else{
					String X = String.format("%.3f", coords[0]);
					String Y = String.format("%.3f", coords[1]);
					System.out.print("["+X+","+Y+"]");
				}
				i.next();
			}
			System.out.println("");
		}

	}

	private void printRect(Rectangle2D r, boolean debug) {
		if(debug) {
			String minX = String.format("%.3f", r.getMinX());
			String minY = String.format("%.3f", r.getMinY());
			String maxX = String.format("%.3f", r.getMaxX());
			String maxY = String.format("%.3f", r.getMaxY());
			String w = String.format("%.3f", r.getMaxX() - r.getMinX() + 1);
			String h = String.format("%.3f", r.getMaxY() - r.getMinY() + 1);
			String msg =
					"R[" + minX + "," + minY  + "]" +
							"[" + maxX + "," + minY  + "]" +
							"[" + maxX + "," + maxY  + "]" +
							"[" + minX + "," + maxY  + "]" +
							"(" + w + "," + h  + ")";
			System.out.println(msg);
		}
	}

}