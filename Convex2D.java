import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.LinkedList;
import java.util.*;

import java.io.PrintWriter;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Convex2D{
	
	public class Point{
		int x;
		int y;
		int[] start;	//reference point used for vector
		int distanceSquare;
		public Point(int x, int y, int[] start){
			this.x = x;
			this.y = y;
			this.start = start;
			distanceSquare = (start[0] - x) * (start[0] - x) + (start[1] - y) * (start[1] - y);
		}
	}
	
	/**
	 * assume input is a list of 2D points, size >= 3
	 * return a list of points (outline)
	 * points is represented with int[]
	 * assume no Overlapping points
	 */
	public List<int[]> findConvexHull(int[][] points){
		
		//incorrect size;
		if(points.length < 3){
			System.out.println("Incorrect input data file. Size needs to be larger than 2!");
			return new ArrayList<int[]>();
		}
		//find the left-bottom most point
		int[] start = findLeftBottomPoint(points);
		Point first = new Point(start[0], start[1], start);
		//convert point from int[] to Point
		List<Point> candidates = getCandidatesList(points, start);
				
		//sort the Point array based on the polar angle of the vector (start -> current)
		sortCandidatesWithPolarAngle(candidates, first);
		if(isALine(candidates, first)){
			System.out.println("Incorrect input data file. All points on a same line.");
			return new ArrayList<int[]>();
		}
		//compute the convex hull outline
		//path a -> b -> c
		//right turn throw pre point b
		//left turn add current point
		//add start point to candidate end to close
		candidates.add(new Point(start[0], start[1], start));
		Deque<Point> stack = new LinkedList<>();
		stack.offerFirst(first);
		stack.offerFirst(candidates.get(0));
		stack.offerFirst(candidates.get(1));
		for(int i = 2; i < candidates.size(); i++){
			Point cur = candidates.get(i);
			Point pre = stack.pollFirst();
			Point prePre = stack.peekFirst();
			while(stack.size() > 1 && isCounterClockWise(prePre, pre, cur) < 0){
				pre = stack.pollFirst();
				prePre = stack.peekFirst();
			}
			stack.offerFirst(pre);
			stack.offerFirst(cur);
		}
		//output the stack, delete the duplicate of start point;
		stack.pollFirst();
		List<int[]> result = new ArrayList<>();
		while(!stack.isEmpty()){
			Point p = stack.pollFirst();
			result.add(new int[]{p.x, p.y});
		}
		return result;
	}
	
	/**
	 * sort implementation for point list
	 */
	public void sortCandidatesWithPolarAngle(List<Point> candidates, Point origin){
		Collections.sort(candidates, new Comparator<Point>(){
			@Override
			public int compare(Point p1, Point p2){
				int isCounter = isCounterClockWise(p1, origin, p2);
				if(isCounter == 0){
					return p1.distanceSquare - p2.distanceSquare;
				}else if(isCounter < 0){	//counter-clockwise sort, p1 first
					return -1;	
				}else{
					return 1;	//clockwise
				}
			}
		});
	}
	
	/**
	 * check if the candidates can form a convex
	 */
	public boolean isALine(List<Point> candidate, Point first){
		Point pre = first;
		for(int i=1; i < candidate.size(); i++){
			if(isCounterClockWise(pre, candidate.get(i-1), candidate.get(i)) != 0){
				return false;
			}
			pre = candidate.get(i-1);
		}
		return true;
	}
	
	/**
	 * compute vector cross product to determine the direction 
	 */
	public int isCounterClockWise(Point p, Point q, Point r){
		int value = (q.x - p.x) * (r.y - p.y) - (q.y - p.y) * (r.x - p.x);
		//value > 0 -> counter; value < 0 clockwise; value == 0 collinear
		if(value == 0){
			return 0;
		}else if(value < 0){
			return -1;
		}else{
			return 1;
		}
	}
	
	/**
	 * convert int[] to Point
	 * return all candidates referring to start point
	 */
	public List<Point> getCandidatesList(int[][] points, int[] start){
		List<Point> candidates = new ArrayList<>();
		for(int[] point:points){
			if(point[0] == start[0] && point[1] == start[1]){
				continue;
			}else{
				Point p = new Point(point[0], point[1], start);
				candidates.add(p);
			}
		}
		return candidates;
	}
	
	/**
	 * find left bottom point
	 */
	public int[] findLeftBottomPoint(int[][] points){
		int[] candidate = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
		for(int[] point : points){
			if(point[0] < candidate[0]){
				candidate[0] = point[0];
				candidate[1] = point[1];
			}else if(point[0] == candidate[0]){
				if(point[1] < candidate[1]){
					candidate[0] = point[0];
					candidate[1] = point[1];
				}
			}
		}
		return candidate;
	}
	
	/**
	 * check if a point is inside a convex
	 * input a convex contour int[][], a point (int[])
	 * convex input may not be sorted
	 * return boolean 
	 */
	public boolean isInsideConvex(int[][] convex, int[] checkPoint){
		//find the leftBottom point from convex
		int[] start = findLeftBottomPoint(convex);
		Point first = new Point(start[0], start[1], start);
		//create list of candidate points
		List<Point> candidates = getCandidatesList(convex, start);
		//sort candidates in a counter-clockwise direction
		sortCandidatesWithPolarAngle(candidates, first);
		//start the convex loop
		candidates.add(0, first);
		//close the convex loop
		candidates.add(new Point(start[0], start[1], start));
		Point target = new Point(checkPoint[0], checkPoint[1], start);
		for(int i=1; i < candidates.size(); i++){
			//if find a right turn, means target is inside the convex
			if(isCounterClockWise(candidates.get(i-1), target, candidates.get(i)) > 0){
				return false;
			}
		}
		return true;
	}
	
	
	//main function
	//handling input and output
	public static void main(String[] args){
		if(args.length == 0){
			System.out.println("Please specify a mode:");
			System.out.println("F(Find): find a convex hull.");
			System.out.println("C(Check): check a point inside a convex or not.");
		}else{
			try{
				if(args[0].equals("F")){
					System.out.println("Reading input");
					File inputfile = new File("input-find.txt");       //read input informations
				    Scanner in = new Scanner(inputfile);
				    PrintWriter out = new PrintWriter("output-find.txt");  //output results after resolution
					//number of test
					int testNumber = Integer.parseInt(in.nextLine());
				    
				    for(int i = 0; i < testNumber; i++){
				    	//number of input points
				    	int row = in.nextInt();
				    	in.nextLine();
				    	int[][] points = new int[row][2];
				    	for(int r = 0; r < row; r++){
				    		points[r][0] = in.nextInt();
				    		points[r][1] = in.nextInt();
				    		if(in.hasNextLine()) in.nextLine();
				    	}
				    	Convex2D test = new Convex2D();
				    	List<int[]> result = test.findConvexHull(points);
				    	//write output to file output.txt
				    	out.println("====================Test case "+ i + ": convex hull====================");
				    	if(result.size() == 0){
				    		out.println("Can not form a correct convex hull! Please check the input data!");
				    	}else{
				    		out.println(result.size());
						    for(int r = 0; r < result.size(); r++){
						        out.println(result.get(r)[0] + " " + result.get(r)[1]);
						    }
				    	}
					    out.println("====================Test case "+ i + ": end=====================");
					    out.println();
				    }
				    System.out.println("Finished computing!!!");
				    in.close();
					out.close();
				    
				}else if(args[0].equals("C")){
					System.out.println("Reading input");
					File inputfile = new File("input-check.txt");       //read input informations
				    Scanner in = new Scanner(inputfile);
				    PrintWriter out = new PrintWriter("output-check.txt");  //output results after resolution
					//number of test
					int testNumber = Integer.parseInt(in.nextLine());
					
					for(int i=0; i < testNumber; i++){
						//number of check point
						int pointNum = Integer.parseInt(in.nextLine());
						List<int[]> points = new ArrayList<>();
						for(int j = 0; j < pointNum; j++){
							int[] point = new int[2];
							point[0] = in.nextInt();
							point[1] = in.nextInt();
							in.nextLine();
							points.add(point);
						}
						//convex data
						int row = in.nextInt();
				    	in.nextLine();
				    	int[][] convex = new int[row][2];
				    	for(int r = 0; r < row; r++){
				    		convex[r][0] = in.nextInt();
				    		convex[r][1] = in.nextInt();
				    		if(in.hasNextLine()) in.nextLine();
				    	}
				    	Convex2D test = new Convex2D();
				    	List<Boolean> results = new ArrayList<>();
				    	out.println("=========Test case: " + i + "============");
				    	for(int j = 0; j < points.size(); j++){
				    		boolean isInside = test.isInsideConvex(convex, points.get(j));
				    		out.println("Point: (" + points.get(j)[0] + "," + points.get(j)[1] + ")->" + isInside);			    		
				    		out.println();
				    	}
				    	out.println("=========Test case: " + i + "============");
					}
					System.out.println("Finished computing!!!");
					System.out.println();
					in.close();
					out.close();
					
				}else{
					System.out.println("Command not found. Please use F for find convex, C for check point position.");
				}
				
			}catch(Exception e){
				System.err.println(e.getMessage());
			}
		}
	}
	
}

