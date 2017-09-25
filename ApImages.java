import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import javax.swing.*;
public class ApImages extends JApplet implements Runnable
{
	String[] img=new String[4];
	int index=-1;
	Thread imageThread;
	Label l1,l2,l3;
	JLabel jl1;
	TextField t1;
	Thread tr;
	JButton jb1;
	ImageIcon image;
	public void init()
{
	img[0]="uno1.jpg";
	img[1]="uno1.jpg";
	img[2]="uno3.jpg";
	img[3]="uno3.jpg";
	l1=new Label("Enter Name:");
	l2=new Label("Welcome to the game of cards!");
	l3=new Label("         ");
	t1=new TextField(20);
	jb1=new JButton("PLAY");
	getContentPane().setLayout(new FlowLayout());
	getContentPane().add(l2);
	getContentPane().add(l3);
	getContentPane().add(l1);
	getContentPane().add(t1);
	getContentPane().add(jb1);
	image=new ImageIcon("uno4.jpg");
	jl1=new JLabel(image);
	getContentPane().add(jl1);
}
	public void start()
{
	tr=new Thread(this);
	tr.start();
}
	public void stop(){
	tr=null;
}
	public void run(){
	while(true) {
	index++;
if(index>3)
index=0;
 image=new ImageIcon(img[index]);
	jl1.setIcon(image);
	try{
	Thread.sleep(500); }
	catch(InterruptedException e){}
}
}
}