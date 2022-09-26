package pdf;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class TRender {

	//param
	boolean cCONNECT_BACKGROUD =true;

	int mDrawID;
	int mPageWidth;
	int mPageHeight;
	List<TRectangle> mOrgRectangleList;
	TreeMap<Integer,List<TRectangle>> mRectangleMap;
	TreeMap<Integer,List<TEdge>> mEdgeMap;
	TreeMap<Integer,List<TRectangle>> mCellMap;


	public void init(int pageWidth, int pageHeight){
		mPageWidth = pageWidth;
		mPageHeight = pageHeight;
		mDrawID = 0;
		mOrgRectangleList = new ArrayList<TRectangle>();
		mRectangleMap = new TreeMap<Integer,List<TRectangle>>();
		mEdgeMap = new TreeMap<Integer,List<TEdge>>();
		mCellMap = new TreeMap<Integer,List<TRectangle>>();
		for(int i = 0; i < pageHeight; i++){
			mRectangleMap.put(i, null);
			mEdgeMap.put(i,null);
			mCellMap.put(i, null);
		}
		TRectangle r = new TRectangle();
		TColor c = new TColor();
		r.setID(mDrawID++);
		r.setSX(0);
		r.setSY(0);
		r.setEX(pageWidth-1);
		r.setEY(pageHeight-1);
		r.setColor(c);
		mOrgRectangleList.add(r);
		List<TRectangle> rl = new ArrayList<TRectangle>();
		rl.add(r);
		mRectangleMap.put(0, rl);
	}

	public void drawRect(int x, int y, int w, int h, int c){
		TRectangle rect = new TRectangle();
		mOrgRectangleList.add(rect);
		rect.setID(mDrawID++);
		rect.setSX(x);
		rect.setSY(y);
		rect.setEX(x + w - 1);
		rect.setEY(y + h - 1);
		rect.setColor(new TColor(c));
		List<TRectangle> list = mRectangleMap.get(y);
		if(list == null){
			list = new ArrayList<TRectangle>();
			list.add(rect);
			mRectangleMap.put(y, list);
		}
		else{
			list.add(0, rect);
		}
	}

	public List<TRectangle>
	getRectangleList(){
		return mOrgRectangleList;
	}

	public TreeMap<Integer,List<TRectangle>>
	getRectangleMap(){
		return mRectangleMap;
	}

	public TreeMap<Integer,List<TEdge>>
	getEdgeMap(){
		return mEdgeMap;
	}

	public TreeMap<Integer,List<TRectangle>>
	getCellMap(){
		return mCellMap;
	}


	// convert to edges
	public void convertToEdge() throws Exception{

		List<TRectangle> activeRectList = null;
		for(int y = 0; y <= mPageHeight; y++) {
			List<TRectangle> nextRectList = mRectangleMap.get(y);
			activeRectList = updateActiveRectList(y, activeRectList, nextRectList);
			List<TEdge> edgeList = genEdgeList(activeRectList);
			mEdgeMap.put(y, edgeList);
		}
		connectNext();
	}

	private void connectNext() {

		for(int y = 0; y <= mPageHeight; y++) {
			List<TEdge> orgList = mEdgeMap.get(y);
			if(orgList == null || orgList.size() <= 1) continue;
			List<TEdge> newList = new ArrayList<TEdge>();
			TEdge e = null;
			TEdge pe = orgList.get(0);
			boolean connect = false;
			for(int i = 1; i < orgList.size(); i++){
				e = orgList.get(i);
				connect = false;
				if(cCONNECT_BACKGROUD) {
					if(pe.checkNext(e)){
						int id = 0;
						if(pe.getID() < e.getID()) id = pe.getID();
						if(e.getID() < pe.getID()) id = e.getID();
						TEdge ne = new TEdge(pe.getSX(), e.getEX(), id, pe.getColor());
						pe = ne;
						connect = true;
					}
					else{
						newList.add(pe);
						pe = e;
					}
				}
				else{
					if(pe.checkNext(e) && pe.getID() != 0 && e.getID() != 0){
						TEdge ne = new TEdge(pe.getSX(), e.getEX(), pe.getID(), pe.getColor());
						pe = ne;
						connect = true;
					}
					else{
						newList.add(pe);
						pe = e;
					}
				}
			}
			if(connect){
				newList.add(pe);
			}
			else if(!connect){
				newList.add(e);
			}
			mEdgeMap.put(y, newList);
		}

	}

	private List<TEdge> genEdgeList(List<TRectangle> active) throws Exception{
		if(active == null){
			return null;
		}

		List<TEdge> edgeList = new ArrayList<TEdge>();
		for(int i = 0; i < active.size(); i++) {
			TRectangle r = active.get(i);
			TEdge e = new TEdge(r.getSX(), r.getEX(), r.getID(), r.getColor());
			edgeList = addEdge(edgeList, e);
		}

		return edgeList;
	}

	private List<TEdge> addEdge(List<TEdge> edgeList, TEdge e) throws Exception{


		if(edgeList.size() == 0){
			edgeList.add(e);
			return edgeList;
		}

		TreeMap<Integer, TEdge> work = new TreeMap<Integer, TEdge>();

		for (int i = 0; i < edgeList.size(); i++) {
			TEdge we = edgeList.get(i);
			work.put(we.getSX(), we);
		}

		for (int i = 0; i < edgeList.size(); i++) {
			TEdge we = edgeList.get(i);

			// UP(WE) -----------
			// DW(E )  --------
			if(e.getSX() >= we.getSX() && e.getEX() <= we.getEX()) {
				e = null;
				break;
			}
			// UP(WE)             --------
			// DW(E )  --------
			else if(e.getEX() < we.getSX()) {
				work.put(e.getSX(), e);
				e = null;
				break;
			}
			// UP(WE)    --------
			// DW(E )  --------
			else if(e.getSX() < we.getSX() && e.getEX() < we.getEX()) {
				TEdge ne = new TEdge(e.getSX(), we.getSX()-1, e.getID(), e.getColor());
				work.put(ne.getSX(), ne);
				e = null;
				break;
			}
			// UP(WE)  --------
			// DW(E )           --------
			else if(e.getSX() > we.getEX()){
				;
			}
			// UP(WE)    ------
			// DW(E )  ----------
			else if(e.getSX() < we.getSX() && e.getEX() > we.getEX()) {
				TEdge ne = new TEdge(e.getSX(), we.getSX()-1, e.getID(), e.getColor());
				work.put(ne.getSX(), ne);
				ne = new TEdge(we.getEX()+1, e.getEX(), e.getID(), e.getColor());
				e = ne;
			}
			// UP(WE)      ------
			// DW(E )  ----------
			else if(e.getSX() < we.getSX() && e.getEX() == we.getEX()) {
				TEdge ne = new TEdge(e.getSX(), we.getSX()-1, e.getID(), e.getColor());
				work.put(ne.getSX(), ne);
				e = null;
				break;
			}
			// UP(WE)  ------
			// DW(E )  ----------
			else if(e.getSX() == we.getSX() && e.getEX() > we.getEX()) {
				TEdge ne = new TEdge(we.getEX()+1, e.getEX(), e.getID(), e.getColor());
				e = ne;
			}
			// UP(WE)  -----
			// DW(E )    ----------
			else if(e.getSX() > we.getSX() && e.getEX() > we.getEX()) {
				TEdge ne = new TEdge(we.getEX()+1, e.getEX(), e.getID(), e.getColor());
				e = ne;
			}
			else{
				throw new Exception("anything wrong.");
			}

		}

		if(e != null){
			work.put(e.getSX(), e);
		}

		List<TEdge> result = new ArrayList<TEdge>();
		for (Integer key : work.keySet()) {
			result.add(work.get(key));
		}

		return result;
	}

	private List<TRectangle> updateActiveRectList(int y, List<TRectangle> active, List<TRectangle> next){

		if(active == null && next == null) {
			return null;
		}
		else if(active == null) {
			return next;
		}
		else if(next == null) {
			;
		}
		else{
			for(int i = 0; i < next.size(); i++) {
				TRectangle nr = next.get(i);
				int nID = nr.getID();
				if(nID > active.get(0).getID()){
					active.add(0, nr);
				}
				else if(nID < active.get(active.size()-1).getID()){
					active.add(nr);
				}
				else{
					for(int j = 0; j < active.size()-1 ; j++) {
						if(nID < active.get(j).getID() && nID > active.get(j+1).getID()){
							active.add(j + 1,nr);
							break;
						}
					}
				}
			}
		}

		List<TRectangle> result = new ArrayList<TRectangle>();

		for(int i = 0; i < active.size(); i++) {
			TRectangle r = active.get(i);
			if(y >= r.getSY() && y <= r.getEY()){
				result.add(active.get(i));
			}
		}

		if(result.size() == 0){
			return null;
		}

		return result;

	}

	//extract Rect
	public void extractCell() {

		TreeMap<Integer,List<TEdge>> edgeMap = copyEdgeMap(mEdgeMap);
		for (int y = 0; y < mPageHeight; y++) {
			List<TRectangle> cells = new ArrayList<TRectangle>();
			while(true) {
				TRectangle rect = detectRect(y, edgeMap);
				if(rect == null) break;
				cells.add(rect);
			}
			if(cells.size() != 0){
				mCellMap.put(y, cells);
			}
		}

	}

	private TreeMap<Integer,List<TEdge>> copyEdgeMap(TreeMap<Integer,List<TEdge>> map){
		TreeMap<Integer,List<TEdge>> result = new TreeMap<Integer,List<TEdge>>();
		for (int y = 0; y < mPageHeight; y++) {
			List<TEdge> list = mEdgeMap.get(y);
			result.put(y, copyList(list));
		}
		return result;
	}

	private List<TEdge> copyList(List<TEdge> list){
		ArrayList<TEdge> result = new ArrayList<TEdge>();
		for(TEdge e : list){
			result.add(e);
		}
		return result;
	}

	private TRectangle detectRect(int y, TreeMap<Integer,List<TEdge>> edgeMap) {

		List<TEdge> edgeList = edgeMap.get(y);
		if(edgeList == null || edgeList.size() == 0){
			return null;
		}
		TEdge e = edgeList.get(0);
		List<TEdge> rectEdges = collectEdges(y, e, edgeMap);
		int height = rectEdges.size();

		TRectangle rect = new TRectangle();
		rect.setSX(e.getSX());
		rect.setEX(e.getEX());
		rect.setSY(y);
		rect.setEY(y + height - 1);
		rect.setColor(e.getColor());
		rect.setID(e.getID());

		deleteEdges(y, rectEdges, edgeMap);

		return rect;
	}

	private List<TEdge> collectEdges(int y, TEdge e, TreeMap<Integer,List<TEdge>> edgeMap){

		ArrayList<TEdge> result = new ArrayList<TEdge>();
		result.add(e);
		for(int i = y + 1; i < mPageHeight; i++) {
			List<TEdge> edgeList = edgeMap.get(i);
			if(edgeList == null || edgeList.size() == 0){
				break;
			}
			boolean find = false;
			for(TEdge te : edgeList){
				if((te.getID() != 0 && e.getID() == 0) || (te.getID() == 0 && e.getID() != 0))  {
					continue;
				}
				else if(te.equalsX(e)){
					result.add(te);
					find = true;
					break;
				}
			}
			if(!find) break;
		}

		return result;
	}

	private void deleteEdges(int y, List<TEdge> edges, TreeMap<Integer,List<TEdge>> edgeMap){

		if(edges == null || edges.size() == 0) {
			return;
		}

		for(int i = 0; i < edges.size(); i++) {
			TEdge delEdge = edges.get(i);
			List<TEdge> edgeList = edgeMap.get(y + i);
			if(edgeList == null || edgeList.size() == 0){
				break;
			}
			for(int j = 0; j < edgeList.size(); j++) {
				if(delEdge.equals(edgeList.get(j))){
					edgeList.remove(j);
					if(edgeList.size() == 0){
						edgeMap.put(j, null);
					}
					break;
				}
			}
		}
	}
}
