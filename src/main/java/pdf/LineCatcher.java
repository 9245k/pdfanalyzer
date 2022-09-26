package pdf;

// https://www.codetd.com/en/article/13873339

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

public class LineCatcher extends PDFGraphicsStreamEngine
{
	private static final GeneralPath mLinePath = new GeneralPath();
	private static ArrayList<Rectangle2D> mRectList= new ArrayList<Rectangle2D>();
	private int mClipWindingRule = -1;
	private static String mHeaderRecord = "Text|Page|x|y|width|height|space|font";

	public LineCatcher(PDPage page)
	{
		super(page);
	}


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
				System.out.println("Line Processing numPages:" + numPages);
				for (int n = 0; n < numPages; n++) {
					System.out.println("Line Processing page:" + n);
					mRectList = new ArrayList<Rectangle2D>();
					PDPage page = document.getPage(n);
					page_height = page.getCropBox().getUpperRightY();
					LineCatcher lineCatcher = new LineCatcher(page);
					lineCatcher.processPage(page);

					try{
						for(Rectangle2D rect:mRectList) {

							String pageNum = Integer.toString(n + 1);
							String x = Double.toString(rect.getX());
							String y = Double.toString(page_height - rect.getY()) ;
							String w = Double.toString(rect.getWidth());
							String h = Double.toString(rect.getHeight());
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

	public List<Rectangle2D> getRects(){
		return mRectList;

	}

	@Override
	public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException
	{
		System.out.println("appendRectangle"+p0.toString()+","+p1.toString()+","+p2.toString()+","+p3.toString());
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
		System.out.println("clip:windingRule="+mClipWindingRule);
		// the clipping path will not be updated until the succeeding painting operator is called
		mClipWindingRule = windingRule;

	}

	@Override
	public void moveTo(float x, float y) throws IOException
	{
		System.out.println("moveTo:"+x+","+y);
		mLinePath.moveTo(x, y);
	}

	@Override
	public void lineTo(float x, float y) throws IOException
	{
		System.out.println("lineTo:"+x+","+y);
		mLinePath.lineTo(x, y);
	}

	@Override
	public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException
	{
		System.out.println("curveTo:"+x1+","+y1+","+x2+","+y1);
		mLinePath.curveTo(x1, y1, x2, y2, x3, y3);
	}

	@Override
	public Point2D getCurrentPoint() throws IOException
	{
		System.out.println("getCurrentPoint:");
		return mLinePath.getCurrentPoint();
	}

	@Override
	public void closePath() throws IOException
	{
		System.out.println("closePath:");
		mLinePath.closePath();
	}

	@Override
	public void endPath() throws IOException
	{
		System.out.println("endPath:windingRule="+mClipWindingRule);
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
		//mRectList.add(mLinePath.getBounds2D());
		mLinePath.reset();
	}

	@Override
	public void fillPath(int windingRule) throws IOException
	{
		System.out.println("fillPath:*rectList*");
		mRectList.add(mLinePath.getBounds2D());
		System.out.println("fillPath:windingRule="+mClipWindingRule);
		mLinePath.reset();
	}

	@Override
	public void fillAndStrokePath(int windingRule) throws IOException
	{
		System.out.println("fillAndStrokePath:*rectList*");
		mRectList.add(mLinePath.getBounds2D());
		System.out.println("fillAndStrokePath:windingRule="+mClipWindingRule);
		mLinePath.reset();
	}

	@Override
	public void shadingFill(COSName cosn) throws IOException
	{
		System.out.println("shadingFill:");
	}

	/**
	 * This will print the usage for this document.
	 */
	private static void usage()
	{
		System.err.println( "Usage: java " + LineCatcher.class.getName() + " <input-pdf>"  + " <output-file>");
	}


}