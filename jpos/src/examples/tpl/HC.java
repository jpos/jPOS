package tpl;

/**
 * HC - Hot Card (present in "Negative File" or "Stop List")
 */
public class HC {
    public String pan = null;
    
    public HC () {
	super();
    }
    public HC (String pan) {
	super();
	this.pan = pan;
    }
    public void setPan (String pan) {
	this.pan = pan;
    }
    public String getPan () {
	return pan;
    }
}

