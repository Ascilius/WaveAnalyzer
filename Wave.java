import java.awt.Color;
import java.util.ArrayList;

public class Wave {

	// characteristics
	Color color;
	double amplitude, angularFrequency, phaseShift, verticalShift; // pix, pix/s, radians?, pix
	boolean sinWave;

	// points
	ArrayList<double[]> points = new ArrayList<double[]>(); // size should be = frame

	public Wave(Color color, double amplitude, boolean sinWave, double angularFrequency, double phaseShift, double verticalShift) {
		this.color = color;
		this.amplitude = amplitude;
		this.sinWave = sinWave;
		this.angularFrequency = angularFrequency;
		this.phaseShift = phaseShift;
		this.verticalShift = verticalShift;
	}

	public void genPoint(double time) {
		double[] point = new double[2];
		point[0] = time;
		if (sinWave == true) {
			point[1] = amplitude * Math.sin(angularFrequency * time + phaseShift) + verticalShift;
		} else {
			point[1] = amplitude * Math.cos(angularFrequency * time + phaseShift) + verticalShift;
		}
		points.add(point);
	}

	public Color getColor() {
		return color;
	}

	public ArrayList<double[]> getPoints() {
		return points;
	}

	public void reset() {
		points.clear();
	}

}
