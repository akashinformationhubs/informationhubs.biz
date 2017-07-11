package com.statestreet;

import java.io.Serializable;
import java.sql.Date;


public class CollateralSsi implements Serializable {
	
	 private String brokerid; 
	 private String product_id;           
	 private String setloc;   
	 private String houseacc;          
	 private String brokeraccno;  
	 private String exebroker;    
	 private String clearbroker;     
	 private String executebic;       
	 private String clearbic;        
	 private String fedmenmonic;        
	 private String ssistatus;          
	 private String createby;           
	 private Date createddatetime;      
	 private String lastmodfied;       
	 private String lastmodifiedtime;      
	 private String approvedby;            
	 private Date  approveddatetime;      
	 private String brokersubaccount;     
	 private String	depositorypartcode;
	 private String seqno;               
	 private String activityflag;
	public String getBrokerid() {
		return brokerid;
	}
	public void setBrokerid(String brokerid) {
		this.brokerid = brokerid;
	}
	public String getProduct_id() {
		return product_id;
	}
	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}
	public String getSetloc() {
		return setloc;
	}
	public void setSetloc(String setloc) {
		this.setloc = setloc;
	}
	public String getHouseacc() {
		return houseacc;
	}
	public void setHouseacc(String houseacc) {
		this.houseacc = houseacc;
	}
	public String getBrokeraccno() {
		return brokeraccno;
	}
	public void setBrokeraccno(String brokeraccno) {
		this.brokeraccno = brokeraccno;
	}
	public String getExebroker() {
		return exebroker;
	}
	public void setExebroker(String exebroker) {
		this.exebroker = exebroker;
	}
	public String getClearbroker() {
		return clearbroker;
	}
	public void setClearbroker(String clearbroker) {
		this.clearbroker = clearbroker;
	}
	public String getExecutebic() {
		return executebic;
	}
	public void setExecutebic(String executebic) {
		this.executebic = executebic;
	}
	public String getClearbic() {
		return clearbic;
	}
	public void setClearbic(String clearbic) {
		this.clearbic = clearbic;
	}
	public String getFedmenmonic() {
		return fedmenmonic;
	}
	public void setFedmenmonic(String fedmenmonic) {
		this.fedmenmonic = fedmenmonic;
	}
	public String getSsistatus() {
		return ssistatus;
	}
	public void setSsistatus(String ssistatus) {
		this.ssistatus = ssistatus;
	}
	public String getCreateby() {
		return createby;
	}
	public void setCreateby(String createby) {
		this.createby = createby;
	}
	public Date getCreateddatetime() {
		return createddatetime;
	}
	public void setCreateddatetime(Date createddatetime) {
		this.createddatetime = createddatetime;
	}
	public String getLastmodfied() {
		return lastmodfied;
	}
	public void setLastmodfied(String lastmodfied) {
		this.lastmodfied = lastmodfied;
	}
	public String getLastmodifiedtime() {
		return lastmodifiedtime;
	}
	public void setLastmodifiedtime(String lastmodifiedtime) {
		this.lastmodifiedtime = lastmodifiedtime;
	}
	public String getApprovedby() {
		return approvedby;
	}
	public void setApprovedby(String approvedby) {
		this.approvedby = approvedby;
	}
	public Date getApproveddatetime() {
		return approveddatetime;
	}
	public void setApproveddatetime(Date approveddatetime) {
		this.approveddatetime = approveddatetime;
	}
	public String getBrokersubaccount() {
		return brokersubaccount;
	}
	public void setBrokersubaccount(String brokersubaccount) {
		this.brokersubaccount = brokersubaccount;
	}
	public String getDepositorypartcode() {
		return depositorypartcode;
	}
	public void setDepositorypartcode(String depositorypartcode) {
		this.depositorypartcode = depositorypartcode;
	}
	public String getSeqno() {
		return seqno;
	}
	public void setSeqno(String seqno) {
		this.seqno = seqno;
	}
	public String getActivityflag() {
		return activityflag;
	}
	public void setActivityflag(String activityflag) {
		this.activityflag = activityflag;
	} 				

}
