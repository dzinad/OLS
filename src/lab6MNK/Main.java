package lab6MNK;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class MNKFrame extends JFrame{
	
	private GraphicPanel graphicPanel;
	private ControlsPanel controlsPanel;
	private String fontFamily = "Serif";
	private int fontStyle = Font.PLAIN;
	private Approximator approximator;
	
	
	public MNKFrame(String s) {
		super(s);
		approximator = new Approximator();
		setLayout(new FlowLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void addGraphic() {
		graphicPanel = new GraphicPanel((int) (getWidth() / 1.5), (int) (getHeight() * 0.9));
		add(graphicPanel);
	}
	
	public void addControlsPanel() {
		controlsPanel = new ControlsPanel((int) (getWidth() / 3) - 50, (int) (getHeight() * 0.9));
		add(controlsPanel);
	}

	
	class GraphicPanel extends JPanel{
		private int screenWidth, screenHeight;
		private double graphicWidth, graphicHeight, scaleX, scaleY;
		private Point2D.Double minBound, maxBound;
		private Point center = new Point();
		private ArrayList<Point2D.Double> points = new ArrayList<>();
		private Point2D.Double highlightPoint = new Point2D.Double();
		private ArrayList<Double> polynom = new ArrayList<>();

		public GraphicPanel() {}
		
		public GraphicPanel(int w, int h) {
			screenWidth = w;
			screenHeight = h;
			setPreferredSize(new Dimension(screenWidth, screenHeight));
			setBackground(Color.WHITE);
			
			drawAxis(-10, 10, -10, 10);
			addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent arg0) {
					double x = (arg0.getX() - center.x) / scaleX;
					double y = (center.y - arg0.getY()) / scaleY;
					Point2D.Double tmp = new Point2D.Double(x, y);
					if (!points.contains(tmp)) {
						points.add(tmp);
						
						//добавим Edit (если есть пустой, просто впишем координаты туда)
						MNKFrame.ControlsPanel.TextEditPanel.AddPointPanel emptyPanel = MNKFrame.this.controlsPanel.textEditPanel.findEmptyPanel();
						if (emptyPanel != null) {
							emptyPanel.setCoords(tmp);
							emptyPanel.requestFocusForX();
						}
						else {
							MNKFrame.this.controlsPanel.textEditPanel.addAddPointPanels(1);
							MNKFrame.this.controlsPanel.textEditPanel.addPointPanels.get(MNKFrame.this.controlsPanel.textEditPanel.addPointPanels.size() - 1).setCoords(tmp);
							MNKFrame.this.controlsPanel.textEditPanel.drawAddPointPanels();
							MNKFrame.this.controlsPanel.textEditPanel.addPointPanels.get(MNKFrame.this.controlsPanel.textEditPanel.addPointPanels.size() - 1).requestFocusForX();
						}
						redraw();
					}
				}				
			});
			
			addMouseMotionListener(new MouseMotionAdapter() {

				@Override
				public void mouseMoved(MouseEvent arg0) {
					boolean overSomePoint = false;
					for (Point2D.Double point: points) {
						if (mouseOver(point, arg0.getX(), arg0.getY(), 4)) {
							if (!point.equals(highlightPoint)) {
								highlightPoint = point;
								MNKFrame.this.controlsPanel.textEditPanel.getPanelByPoint(point).requestFocusForX();
								revalidate();
								repaint();
							}
							overSomePoint = true;
						}
					}
					if (!overSomePoint && highlightPoint != null) {
						highlightPoint = null;
						MNKFrame.this.controlsPanel.textEditPanel.loseFocus();
						revalidate();
						repaint();
					}
				}
				
			});
			
		}

		
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            int additionalX = 5, additionalY = 5;

            g.setColor(Color.BLACK);
            g.drawLine(center.x, additionalY, center.x, screenHeight - additionalY);
            g.drawLine(center.x, additionalY, center.x - 5, additionalY + 10);
            g.drawLine(center.x, additionalY, center.x + 5, additionalY + 10);
            g.drawLine(additionalX, center.y, screenWidth - additionalX, center.y);
            g.drawLine(screenWidth - additionalX, center.y, screenWidth - additionalX - 10, center.y + 5);
            g.drawLine(screenWidth - additionalX, center.y, screenWidth - additionalX - 10, center.y - 5);
            
        	int nodeX = 1;
        	int nodeY = 1;
            if (graphicWidth > screenWidth / 25) {
            	nodeX = (int) Math.ceil(graphicWidth / (screenWidth / 25));
            }
            if (graphicHeight > screenHeight / 25) {
            	nodeY = (int) Math.ceil(graphicHeight / (screenHeight / 25));
            }
            
            for (double x = 1; x <= maxBound.x; x++) {
            	if (x % nodeX == 0) {
                    g.setColor(new Color(219, 219, 219));
                	g.drawLine(center.x + (int) (scaleX*x), additionalY, center.x + (int) (scaleX*x), screenHeight - additionalY);
                    g.setColor(Color.BLACK);
            		g.drawString((int) x+"", center.x + (int) (scaleX*x) - 5, center.y + 15);
            	}
            }
            for (double x = -1; x >= minBound.x; x--) {
            	if (x % nodeX == 0) {
                    g.setColor(new Color(219, 219, 219));
                	g.drawLine(center.x + (int) (scaleX*x), additionalY, center.x + (int) (scaleX*x), screenHeight - additionalY);
                    g.setColor(Color.BLACK);
            		g.drawString((int) x+"", center.x + (int) (scaleX*x) - 5, center.y + 15);
            	}
            }
            for (double y = 1; y <= maxBound.y; y++) {
            	if (y % nodeY == 0){
                    g.setColor(new Color(219, 219, 219));
                	g.drawLine(additionalX, center.y - (int) (scaleY*y), screenWidth - additionalX, center.y - (int) (scaleY*y));
                    g.setColor(Color.BLACK);
                	g.drawString((int) y+"", center.x + 10, center.y - (int) (scaleY*y) + 5);            		
            	}
            }
            for (double y = -1; y >= minBound.y; y--) {
            	if (y % nodeY == 0) {
                    g.setColor(new Color(219, 219, 219));
                	g.drawLine(additionalX, center.y - (int) (scaleY*y), screenWidth - additionalX, center.y - (int) (scaleY*y));
                    g.setColor(Color.BLACK);
                	g.drawString((int) y+"", center.x + 10, center.y - (int) (scaleY*y) + 5);            		
            	}
            }
            g.drawString("0", center.x + 5, center.y + 15);
            
            for (Point2D.Double point: points) {
                g.setColor(Color.RED);
            	if (point.equals(highlightPoint)) {
            		g.setColor(Color.GREEN);
            	}     
            	g.fillOval(center.x + (int) (scaleX * point.x) - 4, center.y - (int) (scaleY * point.y) - 4, 8, 8);
            }
            
            if (points.size() == 0) {
            	return;
            }
            
            if (polynom.size() > 0) {
                g.setColor(Color.BLUE);
                double prevx = minBound.x - 0.6;
                for (double x = minBound.x - 0.5; x <= maxBound.x + 0.5; x += 0.1) {
                    g.drawLine(center.x + (int) (scaleX * prevx), center.y - (int) (scaleY * f(prevx)), center.x + (int) (scaleX * x), center.y - (int) (scaleY * f(x)));
                    prevx = x;
                }            	            	
            }
        }
		
		public double f(double x) {
			double res = 0;
			double mn = 1;
			for (int i = 0; i < polynom.size(); i++) {
				res += polynom.get(i)*mn;
				mn *= x;
			}
			return res;
		}

		public boolean mouseOver(Point2D.Double point, int screenX, int screenY, int offset) {
			int x = (int) (point.x  * scaleX) + center.x;
			int y = center.y - (int) (point.y * scaleY);
			if (screenX <= x + offset && screenX >= x - offset && screenY <= y + offset && screenY >= y - offset) {
				return true;
			}
			return false;
		}
		
		public void redraw() {
			if (points.size() == 0) {
				return;
			}
			double maxX = points.get(0).x;
			double minX = points.get(0).x;
			double maxY = points.get(0).y;
			double minY = points.get(0).y;
			for (Point2D.Double point: points) {
				if (maxX < point.x) {
					maxX = point.x;
				}
				if (maxY < point.y) {
					maxY = point.y;
				}
				if (minX > point.x) {
					minX = point.x;
				}
				if (minY > point.y) {
					minY = point.y;
				}
			}
			drawAxis(minX, maxX, minY, maxY);
			revalidate();
			repaint();			
		}
		
		public void setPolynom(ArrayList<Point2D.Double> pts, ArrayList<Double> plm) {
			points.clear();
			polynom.clear();
			for (Point2D.Double point: pts) {
				points.add(point);
			}
			for (Double koeff: plm) {
				polynom.add(koeff);
			}
		}
		
		public void drawAxis(double minX, double maxX, double minY, double maxY) {
			minBound = new Point2D.Double(Math.min(minX, 0),  Math.min(minY, 0));
			maxBound = new Point2D.Double(Math.max(maxX, 0),  Math.max(maxY, 0));

			int additionalX = screenWidth / 20, additionalY = screenHeight / 20;
			if (minX*maxX < 0) {
				graphicWidth = maxX - minX;
				scaleX = (screenWidth - 2 * additionalX) / graphicWidth;
				center.x = additionalX + (int) (Math.abs(minX)*scaleX);
			}
			else {
				graphicWidth = Math.max(Math.abs(maxX), Math.abs(minX));
				scaleX = (screenWidth - 2 * additionalX) / graphicWidth;
				if (minX <= 0 && maxX <= 0) {
					center.x = additionalX + (int) (scaleX * Math.max(Math.abs(maxX), Math.abs(minX)));
				}
				else {
					center.x = additionalX;
				}
			}
			if (minY*maxY < 0) {
				graphicHeight = maxY - minY;
				scaleY = (screenHeight - 2 * additionalY) / graphicHeight;
				center.y = additionalY + (int) (Math.abs(maxY)*scaleY);
			}
			else {
				graphicHeight = Math.max(Math.abs(maxY), Math.abs(minY));
				scaleY = (screenHeight - 2 * additionalY)/ graphicHeight;
				if (minY >= 0 && maxY >= 0) {
					center.y = additionalY + (int) (scaleY * Math.max(Math.abs(maxY), Math.abs(minY)));
				}
				else {
					center.y = additionalY;
				}
			}
		}
	}

	class ControlsPanel extends JPanel {
		private TextEditPanel textEditPanel;
		private ButtonsPanel buttonsPanel;
		private JScrollPane scrollPane;
		private int width = 0, height = 0, textEditsHeight = 0, textEditsWidth = 0;
		public ControlsPanel() {}
		public ControlsPanel(int w, int h) {
			width = w;
			height = h;
			textEditsHeight = (int) (height*0.75);
			textEditsWidth = (int) (width*0.95);			
			setPreferredSize(new Dimension(w, h));
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			textEditPanel = new TextEditPanel(textEditsWidth, textEditsHeight);
			scrollPane = new JScrollPane(textEditPanel);
			scrollPane.setPreferredSize(new Dimension(textEditsWidth, textEditsHeight));
			scrollPane.setMaximumSize(new Dimension(textEditsWidth, textEditsHeight));
			add(scrollPane);
			add(Box.createVerticalStrut(height/50));
			buttonsPanel = new ButtonsPanel(textEditsWidth, height - textEditsHeight);
			add(buttonsPanel);			
		}
		
		class TextEditPanel extends JPanel {
			private int width, height;
			private boolean editRemoved = false; 
			/*при удалении edit'a было что-то непонятное с фокусом 
			 * (первый edit для x ловил фокус, хотя вроде не должен), поэтому выделялась первая точка.
			 * Чтобы этого избежать, я проверяю при получении фокуса, не получен ли он после удаления edit'a,
			 * для этого переменная editRemoved (лучше не придумала).
			 */
			
			private ArrayList<AddPointPanel> addPointPanels = new ArrayList<>();
			
			public TextEditPanel() {}
			public TextEditPanel(int w, int h) {
				width = w;
				height = h;
				setMinimumSize(new Dimension(width, height));
				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				addAddPointPanels(2);
				drawAddPointPanels();
			}
			
			public AddPointPanel findEmptyPanel() {
				for (AddPointPanel panel: addPointPanels) {
					if (panel.isEmpty()) {
						return panel;
					}
				}
				return null;
			}
			
			public AddPointPanel getPanelByPoint(Point2D.Double p) {
				for (AddPointPanel panel: addPointPanels) {
					if (panel.getPoint().equals(p)) {
						return panel;
					}
				}
				if (addPointPanels.size() > 0) {
					return addPointPanels.get(0);
				}
				return null;
			}
			
			public void addAddPointPanels(int num) {
				for (int i = 0; i < num; i++) {
					addPointPanels.add(new AddPointPanel(width, height/12));
				}
			}
			
			public void drawAddPointPanels() {
				removeAll();
				add(Box.createVerticalStrut(height/50));
				for (int i = 0; i < addPointPanels.size(); i++) {
					add(addPointPanels.get(i));
					add(Box.createVerticalStrut(height/50));					
				}
			}
			
			public void loseFocus() {
				requestFocusInWindow();
			}
			
			class AddPointPanel extends JPanel {
				private int width, height;
				private JTextField editX, editY;
				private JLabel labelX, labelY;
				private JButton button;
				private Point2D.Double point;
				
				public AddPointPanel(int w, int h) {
					width = w;
					height = h;
					setMaximumSize(new Dimension(width, height));
					setBackground(new Color(226, 226, 226));
					labelX = new JLabel("x:");
					labelX.setFont(new Font(fontFamily, fontStyle, height/2));
					labelY = new JLabel("y:");
					labelY.setFont(new Font(fontFamily, fontStyle, height/2));
					editX = new JTextField(7);
					editY = new JTextField(7);
					
					editX.getDocument().putProperty("owner", editX);
					editY.getDocument().putProperty("owner", editY);
					
					editX.setFont(new Font(fontFamily, fontStyle, Math.min(height/3, width/15)));
					editY.setFont(new Font(fontFamily, fontStyle, Math.min(height/3, width/15)));

					FocusListener focusListener = new FocusListener() {
						@Override
						public void focusGained(FocusEvent arg0) {
							((JTextField) arg0.getSource()).setBackground(Color.WHITE);
							if (point != null) {
								if (editRemoved) {
									editRemoved = false;
								}
								else {
									MNKFrame.this.graphicPanel.highlightPoint = point;
									MNKFrame.this.revalidate();
									MNKFrame.this.repaint();									
								}
							}
						}

						@Override
						public void focusLost(FocusEvent arg0) {
							MNKFrame.this.graphicPanel.highlightPoint = null;
						}
						
					};

					DocumentListener coordChanged = new DocumentListener() {

				        @Override
				        public void removeUpdate(DocumentEvent e) {
				        	textChanged(e);			
				        }

				        @Override
				        public void insertUpdate(DocumentEvent e) {
				        	textChanged(e);
				        }

				        @Override
				        public void changedUpdate(DocumentEvent arg0) {
				        }
				        
				        public void textChanged(DocumentEvent e) {
				        	//будем изменять координату точки и перерисовывать оси (чтобы точка точно была видна), но кривуб оставим прежнюю: елси хотим новую, жмем кнопку "Аппроксимировать"
				        	Object source = e.getDocument().getProperty("owner");
							String str = ((JTextField) source).getText();
				        	if (point != null) {
								try {
									double value = Double.parseDouble(str);						
						        	if (source.equals(editX)) {
										point.x = value;
						        	}
						        	else if (source.equals(editY)) {
						        		point.y = value;
						        	}
						        	MNKFrame.this.graphicPanel.redraw();
								}
								catch (NumberFormatException ex) {}								
				        	}
				        	else {
				        		try {
				        			point = parsePoint();
				        		}
				        		catch (NumberFormatException ex) {
				        			return;
				        		}
				        	}
				        	MNKFrame.this.graphicPanel.highlightPoint = point;
				        }
				    };
					
				    editX.getDocument().addDocumentListener(coordChanged);
				    editY.getDocument().addDocumentListener(coordChanged);
				    
					editX.addFocusListener(focusListener);
					editY.addFocusListener(focusListener);

					button = new JButton("—");
					button.setFont(new Font(fontFamily, fontStyle, height/5));
					button.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							editRemoved = true;
							MNKFrame.this.graphicPanel.points.remove(AddPointPanel.this.point);
							
							ControlsPanel.this.textEditPanel.addPointPanels.remove(AddPointPanel.this);
							ControlsPanel.this.textEditPanel.drawAddPointPanels();
							textEditPanel.loseFocus();
							MNKFrame.this.graphicPanel.redraw();
						}
						
					});
					
					add(labelX);
					add(editX);
					add(labelY);
					add(editY);
					add(button);			
				}
				
				public void setPoint(Point2D.Double p) {
					point = p;
				}
				
				public Point2D.Double getPoint() {
					return point;
				}
				
				public Point2D.Double parsePoint() {
					String xstr = editX.getText();
					String ystr = editY.getText();
					double x = 0, y = 0;
					String ex = "";
					try {
						x = Double.parseDouble(xstr);
					}
					catch (NumberFormatException e) {
						ex+="x";
					}
					try {
						y = Double.parseDouble(ystr);
					}
					catch (NumberFormatException e) {
						ex+="y";
					}
					if (ex.equals("")) {
						Point2D.Double r = new Point2D.Double(x, y);
						setPoint(r);
						return r;
						
					}
					else {
						throw new NumberFormatException(ex);
					}
				}
				
				public boolean isEmpty() {
					if (editX.getText().equals("") && editY.getText().equals("")) {
						return true;
					}
					return false;
				}
				
				public void setCoords(Point2D.Double p) {
					editX.setBackground(Color.WHITE);
					editY.setBackground(Color.WHITE);
					editX.setText(new DecimalFormat("#.###", DecimalFormatSymbols.getInstance( Locale.ENGLISH )).format(p.x));
					editY.setText(new DecimalFormat("#.###", DecimalFormatSymbols.getInstance( Locale.ENGLISH )).format(p.y));
					setPoint(p);
				}
				
				public void requestFocusForX() {
					editX.requestFocus();
				}				
			}			
		}
		
		class ButtonsPanel extends JPanel {
			private int width, height;
			private JButton buttonRun, buttonAddPoint;
			private JPanel buttons, comboPanel;
			private JComboBox<Integer> combo;
			private JLabel comboinfo, graphinfo;
			
			public ButtonsPanel(int w, int h) {
				width = w;
				height = h;
				setLayout(new BorderLayout());
				
				buttons = new JPanel();
				buttons.setLayout(new FlowLayout(FlowLayout.CENTER, width / 30, height / 10));
				
				buttonAddPoint = new JButton("Новая точка");
				buttonAddPoint.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						ControlsPanel.this.textEditPanel.addAddPointPanels(1);
						ControlsPanel.this.textEditPanel.drawAddPointPanels();
						revalidate();
						repaint();
					}
					
				});
				
				
				buttonRun = new JButton("Аппроксимировать");
				buttonRun.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						ArrayList<Point2D.Double> points = new ArrayList<>();
						for (int i = 0; i < textEditPanel.addPointPanels.size(); i++) {
							TextEditPanel.AddPointPanel panel = textEditPanel.addPointPanels.get(i);
							try {
								Point2D.Double p = panel.parsePoint();
								if (!points.contains(p)) {
									points.add(p);
								}
							}
							catch (NumberFormatException e) {
								if (e.getMessage().contains("x")) {
									panel.editX.setBackground(Color.RED);
								}
								if (e.getMessage().contains("y")) {
									panel.editY.setBackground(Color.RED);
								}

							}

						}
						
						
						Integer degree = (Integer) buttonsPanel.combo.getSelectedItem();
						
						
						approximator.approximate(points, degree);
						
						//обернем info в <html>...</html> для автоматического переноса строки
						String info = "<html>Кривая: y = ";
						ArrayList<Double> polKoeffs = approximator.getPolynom();
						for (int i = polKoeffs.size() - 1; i >= 0; i--) {
							if (i < polKoeffs.size() - 1) {
								if (polKoeffs.get(i) == 0) {
									continue;
								}
								if (polKoeffs.get(i) > 0) {
									info +="+";
								}
							}				
							
							//DecimalForamt, чтобы он не выводил кучу десятичных цифр
							info += new DecimalFormat("0.###", DecimalFormatSymbols.getInstance( Locale.ENGLISH )).format(polKoeffs.get(i));
							if (i > 0) {
								info += "*x";
								if (i > 1) {
									info += "^" + i;
								}
							}
						}
						info += "</html>";
						graphinfo.setFont(new Font(fontFamily, fontStyle, Math.min(height/12, width/18)));
						graphinfo.setText(info);
						
						MNKFrame.this.graphicPanel.setPolynom(points, approximator.getPolynom());
						MNKFrame.this.graphicPanel.redraw();
					}
						
					
				});
				
				graphinfo = new JLabel(); 
				
				buttonAddPoint.setFont(new Font(fontFamily, fontStyle, Math.min(height/10, width/17)));
				buttonRun.setFont(new Font(fontFamily, fontStyle, Math.min(height/10, width/17)));
				buttons.add(buttonAddPoint);
				buttons.add(buttonRun);
				
				comboPanel = new JPanel();
				comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.Y_AXIS));
				comboinfo = new JLabel("Выберите степень полинома:");
				comboinfo.setFont(new Font(fontFamily, fontStyle, Math.min(height/8, width/15)));
				combo = new JComboBox<>();				
				combo.setFont(new Font(fontFamily, fontStyle, Math.min(height/10, width/13)));
				for (int i = 0; i <= 5; i++) {
					combo.addItem(i);
				}
				comboPanel.add(comboinfo);
				comboPanel.add(combo);
				
				add(comboPanel, BorderLayout.NORTH);
				add(graphinfo, BorderLayout.CENTER);
				add(buttons, BorderLayout.SOUTH);
			}
		}	
	}
}

class Approximator {
	ArrayList<Double> polynom = new ArrayList<>();
	
	public Approximator() {}
	
	public ArrayList<Double> getPolynom() {
		return polynom;
	}
		
	public void approximate(ArrayList<Point2D.Double> points, int degree) {
		polynom.clear();

		double koeffs[][] = new double[degree + 1][degree + 2];
		for (int i = 0; i < Math.min(points.size(), degree + 1); i++) {
			for (int j = 0; j <= degree; j++) {
				double sum = 0;
				for (int k = 0; k < points.size(); k++) {
					sum += Math.pow(points.get(k).x, i+j); 
				}
				koeffs[i][j] = sum;
			}
			double 	sum = 0;
			for (int k = 0; k < points.size(); k++) {
				sum += points.get(k).y*Math.pow(points.get(k).x, i);
			}
			koeffs[i][degree + 1] = sum;			
		}
		
		for (int i = points.size(); i <= degree; i++) {
			for (int j = 0; j <= degree; j++) {
				if (i == j) {
					koeffs[i][j] = 1;
				}
				else {
					koeffs[i][j] = 0;
				}
			}
			koeffs[i][degree + 1] = 1;
		}
		
		for (int i = 0; i <= degree; i++){
            double gl = koeffs[i][i];
            for (int j = i; j <= degree + 1; j++) {
            	koeffs[i][j] /= gl;
            }
            
            for (int j = i + 1; j <= degree; j++){
                double mn = koeffs[j][i];
                for (int l = i; l <= degree + 1; l++){
                    koeffs[j][l]+=koeffs[i][l]*(-1)*mn;
                }
            }

       }
		
       for (int j = degree; j > 0; j--){
            for (int i = j-1; i >= 0; i--){
                double mn = koeffs[i][j];
                koeffs[i][degree + 1] += koeffs[j][degree + 1]*(-1)*mn;
                koeffs[i][j] += koeffs[j][j]*(-1)*mn;
            }
        }
    

		for (int i = 0; i <= degree; i++) {
			polynom.add(koeffs[i][degree + 1]);
		}
		
	}
}


public class Main {

	public static void main(String[] args) {
		MNKFrame frame = new MNKFrame("Метод наименьших квадратов");
		frame.setSize(new Dimension(1500, 900));
		frame.addGraphic();
		frame.addControlsPanel();
		
		frame.setVisible(true);
	}

}
