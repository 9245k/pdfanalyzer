package pdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;



public class TMain {

	static boolean mDebug = false;
	static boolean mBitmap = true;



	public static int to(double length){
		//return (int) ((72 * length) / 300.0);
		return (int) length;
		//return (int) (length*10.0);
		//return (int) (length/10.0);
	}

	public static void main(String[] args) throws IOException {


		/*
		String[] files = {


				"table/rect.pdf",
				"table/simple.pdf",
				"table/table.pdf",
				"table/019_INVOICE.pdf",
				"table/043_INVOICE.pdf",
				"table/048_INVOICE.pdf",
				"table/072_INVOICE.pdf",
				"table/090_INVOICE.pdf",
				"table/Invoice-028-kanzen-keisen.pdf",
				"table/Quotation-006-kanzen-keisen.pdf",
				"table/Quotation-029-kanzen-keisen.pdf",
				"table/1_01_order_tate.pdf",
				"table/3_34_order-simple.pdf",
				"table/4_41_order01.pdf",
				"table/7_60_order01.pdf",
				"table/9_68_order02.pdf",


		};
		*/

		/*
		String[] files = {


				"1-5/001_INVOICE.pdf",
				"1-5/002_INVOICE.pdf",
				"1-5/003_INVOICE.pdf",
				"1-5/004_INVOICE.pdf",
				"1-5/005_INVOICE.pdf",
				"1-5/001_QUOTATION.pdf",
				"1-5/002_QUOTATION.pdf",
				"1-5/003_QUOTATION.pdf",
				"1-5/004_QUOTATION.pdf",
				"1-5/005_QUOTATION.pdf",



		};
		*/

		String[] files = {



				"048/048_INVOICE.pdf",


		};

		for(String name : files){
			process(name);
		}

	}

	public static void process(String fileName) throws IOException {
		//step1 get rectangles
		List<PDFRectangle> rects = new ArrayList<PDFRectangle>();
		PDPage page = execute(fileName, rects);
		TRender render = new TRender();


		int pw = to(page.getBBox().getWidth());
		int ph = to(page.getBBox().getHeight());
		render.init(pw, ph);
		System.out.println("pageWidth="+pw+" pageHeight="+ph);

		for(PDFRectangle r:rects){
			int minx = to(r.mRectangle2D.getMinX());
			int miny = to(r.mRectangle2D.getMinY());
			int maxx = to(r.mRectangle2D.getMaxX());
			int maxy = to(r.mRectangle2D.getMaxY());
			if(minx < 0) minx = 0;
			if(miny < 0) maxy = 0;
			if(maxx >= pw) maxx = pw - 1;
			if(maxy >= ph) maxy = ph - 1;
			int w = maxx - minx + 1;
			int h = maxy - miny + 1;
			if(mDebug) {
				String msg =
						"D[" + minx + "," + miny  + "]" +
								"[" + maxx + "," + miny  + "]" +
								"[" + maxx + "," + maxy  + "]" +
								"[" + minx + "," + maxy  + "]" +
								"(" + w + "," + h  + ")";
				System.out.println(msg);
			}
			render.drawRect(minx, ph - miny - h, w, h, r.mFillColor.toRGB());
			//render.drawRect(minx, miny, w, h, r.mFillColor.toRGB());
		}
		if(mDebug) TRectangleUtil.printRects(render.getRectangleList());
		//if(mBitmap) TRectangleUtil.RectToBitmap(pw, ph, render.getRectangleList(), fileName + ".v");

		// step2 rendering and edges
		try {
			render.convertToEdge();
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		if(mDebug) TRectangleUtil.PrintEdges(render.getEdgeMap());
		if(mBitmap) TRectangleUtil.EdgeToBitmap(pw, ph, render.getEdgeMap(), fileName + ".e");

		// step3 extract rectangles
		render.extractCell();
		if(mDebug) TRectangleUtil.printRects(render.getCellMap());
		//if(mBitmap) TRectangleUtil.RectToBitmap(pw, ph, render.getCellMap(), fileName + ".r");

		//step4 extract table frame
		TAnalyzer analyzer  = new TAnalyzer(pw, ph, new TColor());
		List<List<TRectangle>> frames = analyzer.extractTableFrame(render.getCellMap());
		if(mBitmap) {
			for(int i = 0;i < frames.size(); i++) {
				TRectangleUtil.RectToBitmap(pw, ph, frames.get(i), fileName + "." + (i+1) + ".f");
			}
		}

		//step5 extract table-cells
		for(int i = 0;i < frames.size(); i++) {
			List<TRectangle> cells = analyzer.extractCell(render.getCellMap(), frames.get(i));
			TRectangleUtil.RectToBitmap2(pw, ph, cells, fileName + "." + (i+1) + ".c");
		}


	}


	public static PDPage execute(String filename, List<PDFRectangle> rects) throws IOException{

		PDDocument document = null;
		PDPage page = null;
		int numPages;
		double page_height = 0;
		try
		{
			document = PDDocument.load( new File(filename));
			//numPages = document.getNumberOfPages();
			numPages = 1;

			for (int n = 0; n < numPages; n++) {
				System.out.println("Line Processing page:" + n);
				page = document.getPage(n);
				page_height = page.getCropBox().getUpperRightY();
				PDFLineCatcher lineCatcher = new PDFLineCatcher(page);
				lineCatcher.processPage(page);
				reverseY(lineCatcher.getRects(), rects, page_height);
			};

		}
		catch(IOException io){
			throw new IOException("Failed to Parse document for line processing. Incorrect document format.");
		}
		finally
		{
			if( document != null )
			{
				document.close();
			}
		}

		return page;

	}

	public static void reverseY(List<PDFRectangle> from, List<PDFRectangle> to, double page_height){

		for(PDFRectangle rect:from) {
			to.add(rect);
		}

		/*
		for(PDFRectangle rect:from) {
			double x = rect.mRectangle2D.getX();
			//double y = page_height - rect.getY();
			double y = rect.mRectangle2D.getY();
			double w = rect.mRectangle2D.getWidth();
			double h = rect.mRectangle2D.getHeight();
			PDFRectangle r = new PDFRectangle();
			r.mRectangle2D = new Rectangle2D.Double(x, y, w, h);
			to.add(r);
		}
		*/
	}

}
