package pdf;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class TAnalyzer {



	boolean mDebug1 = false;

	//param
	double cTHIN_RATE = 0.005;   //frame rects rate of page size
	int cFAT_RATE = 20;  //
	double cMAX_FRAME_RANGE = 0.9;  //max len range in frame
	int cMIN_NUM_OF_CELL = 4;


	int mWidth = -1;
	int mHeight = -1;
	TColor mBackgroudColor = null;
	int mTHIN  = 0;
	int mFAT = 0;

	class Area {
		int mMinX;
		int mMinY;
		int mMaxX;
		int mMaxY;
		Area(int minX, int minY, int maxX, int maxY){
			mMinX = minX;
			mMinY = minY;
			mMaxX = maxX;
			mMaxY = maxY;
		}
		int size(){
			return ((mMaxX - mMinX + 1) * (mMaxY - mMinY + 1));
		}
	};

	public TAnalyzer(int w, int h, TColor back){
		mWidth = w;
		mHeight = h;
		mTHIN = (int)((double)(mHeight) * cTHIN_RATE);
		mFAT = mTHIN * cFAT_RATE;
		mBackgroudColor = back;

		System.out.println("THIN="+mTHIN+" FAT="+mFAT);

	}

	public List<TRectangle>
	extractCell(TreeMap<Integer,List<TRectangle>> cellMap, List<TRectangle> frame){
		if(frame == null || frame.size() == 0 || cellMap == null || cellMap.size() == 0) {
			return null;
		}

		List<TRectangle> result = new ArrayList<TRectangle>();
		for(int i = 0; i < cellMap.size(); i++) {
			if(cellMap.get(i) == null || cellMap.get(i).size() == 0) {
				continue;
			}
			result.addAll(cellMap.get(i));
		}

		Area area = calcArea(frame);
		result = search(result, area);
		result = sub(result, frame);

		return result;
	}

    private List<TRectangle>
    search(List<TRectangle> list, Area area) {
		List<TRectangle> result = new ArrayList<TRectangle>();
    	for(TRectangle r : list) {
    		if(r.getSX() >= area.mMinX && r.getEX() <= area.mMaxX &&
    				r.getSY() >= area.mMinY && r.getEY() <= area.mMaxY) {
    			result.add(r);
    		}
    	}
    	return result;
    }

    private List<TRectangle>
    sub(List<TRectangle> from, List<TRectangle> target) {
		List<TRectangle> result = new ArrayList<TRectangle>();
    	for(TRectangle r1 : from) {
    		boolean same = false;
    		for(TRectangle r2 : target) {
    			if(r1.checkSame(r2)){
    				same = true;
    				break;
    			}
    		}
    		if(!same){
    			result.add(r1);
    		}
    	}
    	return result;
    }

	public List<List<TRectangle>>
	extractTableFrame(TreeMap<Integer,List<TRectangle>> cellMap){

		TreeMap<Integer,List<TRectangle>> frameMap = copyMap(cellMap);


		if(mDebug1) {
			//return all frame to debug.
			frameMap = filterThinRect(frameMap);
			frameMap = filterForgroundRect(frameMap);
			List<TRectangle> maxLenLists = new ArrayList<TRectangle>();
			for(int i = 0; i < frameMap.size(); i++){
				List<TRectangle> list = frameMap.get(i);
				if(list == null) continue;
				maxLenLists.addAll(list);
			}
			List<List<TRectangle>> result = new ArrayList<List<TRectangle>>();
			result.add(maxLenLists);
			return result;
		}
		else{
			frameMap = filterThinRect(frameMap);
			frameMap = filterForgroundRect(frameMap);
		}

		//deceide firstVerticalRects
		int maxLen = -1;

		for(int i = 0; i < frameMap.size(); i++){
			List<TRectangle> list = frameMap.get(i);
			if(list == null) continue;
			for(int j = 0; j < list.size(); j++){
				if(list.get(j).getW() >= maxLen){
					maxLen = list.get(j).getW();
				}
			}
		}

		List<TRectangle> detectLists = new ArrayList<TRectangle>();
		for(int i = 0; i < frameMap.size(); i++){
			List<TRectangle> list = frameMap.get(i);
			if(list == null) continue;
			for(int j = 0; j < list.size(); j++){
				if(list.get(j).getW() >= maxLen * cMAX_FRAME_RANGE){
					detectLists.add(list.get(j));
				}
			}
		}


		int maxAreaSize= 0;
		List<List<TRectangle>> detectAreaLists = new ArrayList<List<TRectangle>>();
		for(int i = 0; i < detectLists.size(); i++) {

			boolean hasDone = false;
			for(List<TRectangle> ltr: detectAreaLists) {
				if(contains(ltr, detectLists.get(i))) {
					hasDone = true;
					break;
				}
			}
			if(hasDone) continue;

			TreeMap<Integer,List<TRectangle>> tmpMap = copyMap(frameMap);
			List<TRectangle> result = collectFrame(detectLists.get(i), tmpMap);
			detectAreaLists.add(result);
			int tmp = calcFrameArea(result);
			if(tmp > maxAreaSize) {
				maxAreaSize = tmp;
				System.out.println("max area = " + maxAreaSize);

			}
		}
		List<List<TRectangle>> maxAreaResults = new ArrayList<List<TRectangle>>();
		for(List<TRectangle> ltr: detectAreaLists) {
			Area tmp = calcArea(ltr);
			if(tmp.size() >= maxAreaSize * cMAX_FRAME_RANGE){
				maxAreaResults.add(ltr);
			}
		}

		return maxAreaResults;


	}

	private Area calcArea(List<TRectangle> rects) {
		if(rects == null || rects.size() == 0) {
			return null;
		}
		int minx = 100000;
		int miny = 100000;
		int maxx = -1;
		int maxy = -1;
		for(TRectangle tr : rects){
			if(tr.getSX() < minx) minx = tr.getSX();
			if(tr.getEX() > maxx) maxx = tr.getEX();
			if(tr.getSY() < miny) miny = tr.getSY();
			if(tr.getEY() > maxy) maxy = tr.getEY();
		}
		return new Area(minx, miny, maxx, maxy);
	}

	private int calcFrameArea(List<TRectangle> rects) {
		if(rects == null || rects.size() == 0) {
			return 0;
		}
		int minx = 100000;
		int miny = 100000;
		int maxx = -1;
		int maxy = -1;
		for(TRectangle tr : rects){
			if(tr.getSX() < minx) minx = tr.getSX();
			if(tr.getEX() > maxx) maxx = tr.getEX();
			if(tr.getSY() < miny) miny = tr.getSY();
			if(tr.getEY() > maxy) maxy = tr.getEY();
		}
		return (maxx - minx + 1)*(maxy - miny + 1);
	}

	private boolean contains(List<TRectangle> rects, TRectangle target) {
		for(TRectangle r : rects) {
			if(r.checkSame(target)) {
				return true;
			}
		}
		return false;
	}

	private List<TRectangle> collectFrame(TRectangle start, TreeMap<Integer,List<TRectangle>> map) {

		if(start.getW() < mFAT){
			System.out.println("Frame not found.");
			return null;
		}

		List<TRectangle> frameRects = new ArrayList<TRectangle>();
		frameRects.add(start);
		List<TRectangle> tl = map.get(start.getSY());
		for(int i = 0; i < tl.size(); i++){
			if(start.checkSame(tl.get(i))){
				tl.remove(i);
				break;
			}
		}

		boolean find = true;
		int safeCount = 1000;
		while(find){
			find = findLinked(map, frameRects);
			if(safeCount-- == 0) break;
		}

		return frameRects;
	}


	private boolean findLinked(TreeMap<Integer,List<TRectangle>> map, List<TRectangle> findList){

		for(int i = 0; i < map.size(); i++){
			List<TRectangle> list = map.get(i);
			if(list == null) continue;
			for(int j = 0; j < list.size(); j++) {
				if(next(list.get(j), findList)){
					findList.add(list.get(j));
					//System.out.println("link "+list.get(j).printString());
					list.remove(j);
					if(list.size() == 0){
						map.put(i, null);
					}
					return true;
				}
			}
		};

		return false;
	}

	private boolean next(TRectangle from, List<TRectangle> findList){
		for(TRectangle r : findList){
			if(next(from , r) || next(r, from)){
				//System.out.println("");
				//System.out.println(from.printString());
				//System.out.println(r.printString());
				return true;
			}
		}
		return false;
	}

	// check connected from → to or from ↓ to
	private boolean next(TRectangle from, TRectangle to)
	{
		if(from.getEX() + 1 == to.getSX()){

			// not case
			//  ----
			//  |f |
			//  |  |
			//  ----
			//
			//      ----
			//      |t |
			//      |  |
			//      ----
			if(from.getEY() < to.getSY()){
				return false;
			}
			// not case
			//      ----
			//      |t |
			//      |  |
			//      ----
			//
			//  ----
			//  |f |
			//  |  |
			//  ----
			else if(from.getSY() > to.getEY()){
				return false;
			}
			else{
				return true;
			}
		}
		else if(from.getEY() + 1 == to.getSY()){
			//   ----
			//   |f |
			//   |  |
			//   ----
			//          ----
			//          |t |
			//          |  |
			//          ----
			if(from.getEX() < to.getSX()){
				return false;
			}
			//   ----
			//   |f |
			//   |  |
			//   ----
			//          ----
			//          |t |
			//          |  |
			//          ----
			else if(from.getEX() < to.getSX()){
				return true;
			}
			else{
				return true;
			}
		}

		return false;

	}




	private TreeMap<Integer,List<TRectangle>>
	filterThinRect(TreeMap<Integer,List<TRectangle>> cellMap)
	{
		TreeMap<Integer,List<TRectangle>> result
		= new TreeMap<Integer,List<TRectangle>>();

		for(int i = 0; i < cellMap.size(); i++){
			List<TRectangle> list = cellMap.get(i);
			if(list == null) {
				result.put(i, null);
				continue;
			}
			else {
				List<TRectangle> newList = new ArrayList<TRectangle>();
				for(int j = 0; j < list.size(); j++){
					TRectangle r = list.get(j);
					if(r.getH() <= mTHIN || r.getW() <= mTHIN) {
						if(r.getSX() != 0 && r.getEX() < mWidth - 1){
							newList.add(r);
						}
					}
				}
				if(newList.size() > 0){
					result.put(i, newList);
				}
				else{
					result.put(i, null);
				}
			}
		}
		return result;
	}

	private TreeMap<Integer,List<TRectangle>> copyMap(TreeMap<Integer,List<TRectangle>> map){
		TreeMap<Integer,List<TRectangle>> result = new TreeMap<Integer,List<TRectangle>>();
		for (int y = 0; y < mHeight; y++) {
			List<TRectangle> list = map.get(y);
			result.put(y, copyList(list));
		}
		return result;
	}

	private List<TRectangle> copyList(List<TRectangle> list){
		if(list == null) {
			return null;
		}
		ArrayList<TRectangle> result = new ArrayList<TRectangle>();
		for(TRectangle e : list){
			result.add(e);
		}
		return result;
	}


	private TreeMap<Integer,List<TRectangle>>
	filterForgroundRect(TreeMap<Integer,List<TRectangle>> cellMap)
	{
		TreeMap<Integer,List<TRectangle>> result
		= new TreeMap<Integer,List<TRectangle>>();

		for(int i = 0; i < cellMap.size(); i++){
			List<TRectangle> list = cellMap.get(i);
			if(list == null) {
				result.put(i, null);
				continue;
			}
			else {
				List<TRectangle> newList = new ArrayList<TRectangle>();
				for(int j = 0; j < list.size(); j++){
					TRectangle r = list.get(j);
					if(r.getID()!=0){
						newList.add(r);
					}
				}
				if(newList.size() > 0){
					result.put(i, newList);
				}
				else{
					result.put(i, null);
				}
			}
		}
		return result;
	}


}
