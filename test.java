package com.statestreet.sle.lcm.transaction.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import com.statestreet.sle.framework.concurrency.Lock;
import com.statestreet.sle.framework.db.dbservice.IValueListHandler;
import com.statestreet.sle.framework.db.dbservice.SysDateObject;
import com.statestreet.sle.framework.db.util.DateTime;
import com.statestreet.sle.framework.logging.SLEEventLogger;
import com.statestreet.sle.framework.util.SLEResource;
import com.statestreet.sle.framework.valueobject.IValueObject;
import com.statestreet.sle.framework.valueobject.VOFactory;
import com.statestreet.sle.framework.valueobject.ValueObjectException;
import com.statestreet.sle.lcm.transaction.constants.CollateralMessage;
import com.statestreet.sle.lcm.transaction.constants.CollateralState;
import com.statestreet.sle.lcm.transaction.constants.CollateralVO;
import com.statestreet.sle.lcm.transaction.exception.LoanException;

public class CollateralSessionHelper {
	private static final String SOFTLOCKTIMEOUT = "SOFTLOCKTIMEOUT";
	private static final String SOFTLOCKRETRYCOUNT= "SOFTLOCKRETRYCOUNT";
	private static final long ONEDAY = 1000 * 60 * 60 * 24;
	private static final String FALSE = "FALSE";

	public CollateralSessionHelper () {}
	public Long[] getUniqueLoanNumbers (IValueListHandler loans) throws LoanException {
		SLEEventLogger.logDebug (this, "<<Start of method getUniqueLoanNumbers >>");
		try {
			if (loans == null) {
				return null;
			}
			ArrayList loanNumbers = new ArrayList();
			for (int i = 0; i < loans.getSize (); i++) {
				IValueObject loan = loans.get (i);
				Long sleLoanNumber = loan.getLongValue (CollateralVO.TRADENUMBER);
				if (sleLoanNumber == null) {
					throw new LoanException("The SLE Loan Number is null");
				}
				if (!loanNumbers.contains (sleLoanNumber)) {
					loanNumbers.add (sleLoanNumber);
				}
			}
			Collections.sort (loanNumbers);
			SLEEventLogger.logDebug (this, "<<End  of method getUniqueLoanNumbers >>");
			return (Long[]) loanNumbers.toArray (new Long[loanNumbers.size ()]);
		}
		catch (ValueObjectException ove) {
			SLEEventLogger.logError (this, ove, "Error in Loan Processes");
			throw new LoanException("Error getting SLE Loan number", ove);
		}
		catch (Exception e) {
			SLEEventLogger.logError (this, e, "Error in Loan Processes");
			throw new LoanException("Error getting SLE Loan number", e);
		}
	}
	public void aquireLock (Long loanNumbers[]) throws LoanException {
		if (loanNumbers == null) {
			return;
		}
		for (int i = 0; i < loanNumbers.length; i++) {
			if (loanNumbers == null) {
				continue;
			}
			aquireLock (loanNumbers[i]);
		}
	}
	public void aquireLock (Long loanNumber) throws LoanException {
		try {
			Lock theLock = Lock.getInstance ();
			String timeOutStr = SLEResource.getResource (CollateralVO.LOANCONFIG, SOFTLOCKTIMEOUT);
			String retryCountStr = SLEResource.getResource (CollateralVO.LOANCONFIG, SOFTLOCKRETRYCOUNT);
			int timeOut = Integer.parseInt (timeOutStr);
			int retryCount = Integer.parseInt (retryCountStr);
			if (retryCount < 1) {
				retryCount = 1;
			}
			if (timeOut < 100) {
				timeOut = 100;
			}
			SLEEventLogger.logDebug (this, "Locking Loan: " + loanNumber);
			boolean lockResult = false;
			for (int i = 0; ((i < retryCount) && (!lockResult)); i++) {
				lockResult = theLock.attempt (loanNumber, timeOut);
			}
			if (!lockResult) {
				throw new LoanException("Could not aquire lock for loan number: " + loanNumber);
			}
			SLEEventLogger.logDebug (this, "Acquired Lock Loan: " + loanNumber);
		}
		catch (LoanException le) {
			SLEEventLogger.logError (this, le, "Error in Acquiring Lock: " + loanNumber);
			throw le;
		}
		catch (Exception e) {
			SLEEventLogger.logError (this, e, "Error in Acquiring Lock: " + loanNumber);
			throw new LoanException("Error in Acquiring Lock: " + loanNumber, e);
		}
	}
	public void releaseLock (Long loanNumbers[]) throws LoanException {
		if (loanNumbers == null) {
			return;
		}
		for (int i = 0; i < loanNumbers.length; i++) {
			if (loanNumbers == null) {
				continue;
			}
			releaseLock (loanNumbers[i]);
		}
	}
	public void releaseLock (Long loanNumber) throws LoanException {
		try {
			Lock theLock = Lock.getInstance ();
			SLEEventLogger.logDebug (this, "Releasing Lock Loan: " + loanNumber);
			theLock.release (loanNumber);
		}
		catch (Exception e) {
			SLEEventLogger.logError (this, e, "Error in Releasing Lock: " + loanNumber);
		}
	}
	public IValueObject createTradeRecord (IValueObject messageVO) throws LoanException {
		SLEEventLogger.logDebug (this, "<<Start of method createTradeRecord (IValueObject messageVO) >> ");
		try {
			//IValueObject summary = VOFactory.getVO (newActivity.getType ());
			IValueObject trade = VOFactory.getVO ("Collateral");
			trade.setType ("Collateral");
			trade.setValue (CollateralVO.TRADENUMBER, messageVO.getValue (CollateralVO.TRADENUMBER));
			trade.setValue (CollateralVO.DML_AREAID, messageVO.getValue (CollateralVO.DML_AREAID));
			trade.setValue (CollateralVO.BUSLINE, messageVO.getValue (CollateralVO.BUSLINE));
			trade.setValue (CollateralVO.SETLCURR, messageVO.getValue (CollateralVO.SETLCURR));
			trade.setValue (CollateralVO.COLLCURR, messageVO.getValue (CollateralVO.COLLCURR));
			trade.setValue (CollateralVO.CUSIP, messageVO.getValue (CollateralVO.CUSIP));
			trade.setValue (CollateralVO.HOUSEACCOUNT, messageVO.getValue (CollateralVO.HOUSEACCOUNT));
			trade.setValue (CollateralVO.TRADESTATUSCODEDML, messageVO.getValue (CollateralVO.TRADESTATUSCODEDML));
			trade.setValue (CollateralVO.ASOFDATE, messageVO.getValue (CollateralVO.ASOFDATE));
			trade.setValue (CollateralVO.CLOSEDATE, messageVO.getValue (CollateralVO.CLOSEDATE));
			trade.setValue (CollateralVO.SETTLEMENTLOCATION, messageVO.getValue (CollateralVO.SETTLEMENTLOCATION));
			trade.setValue (CollateralVO.PRICE, messageVO.getValue (CollateralVO.PRICE));
			trade.setValue (CollateralVO.QUANTITYDML, messageVO.getValue (CollateralVO.QUANTITYDML));
			trade.setValue (CollateralVO.AMOUNTDML, messageVO.getValue (CollateralVO.AMOUNTDML));
			trade.setValue (CollateralVO.CASHWASHAMOUNT, messageVO.getValue (CollateralVO.CASHWASHAMOUNT));
			trade.setValue (CollateralVO.SETTLEDFLAG, messageVO.getValue (CollateralVO.SETTLEDFLAG));
			trade.setValue (CollateralVO.TICKETPRINTFLAG, messageVO.getValue (CollateralVO.TICKETPRINTFLAG));
			trade.setValue (CollateralVO.ORIGINATOR, messageVO.getValue (CollateralVO.ORIGINATOR));
			trade.setValue (CollateralVO.LASTACTIVITYASOFDATE, messageVO.getValue (CollateralVO.LASTACTIVITYASOFDATE));
			trade.setValue (CollateralVO.LASTACTIVITYPROCESSINGDATE, messageVO.getValue (CollateralVO.LASTACTIVITYPROCESSINGDATE));
			trade.setValue (CollateralVO.LASTACTIVITYSEQUENCE, messageVO.getValue (CollateralVO.LASTACTIVITYSEQUENCE));
			trade.setValue (CollateralVO.LASTACTIVITYTOE, messageVO.getValue (CollateralVO.LASTACTIVITYTOE));
			trade.setValue (CollateralVO.LOCATIONCODE, messageVO.getValue (CollateralVO.LOCATIONCODE));
			trade.setValue (CollateralVO.REMARKSDML, messageVO.getValue (CollateralVO.REMARKSDML));
			trade.setValue (CollateralVO.POOLID, messageVO.getValue (CollateralVO.POOLID));
			trade.setValue (CollateralVO.CREATEDATEDML, messageVO.getValue (CollateralVO.CREATEDATEDML));
			trade.setValue (CollateralVO.COLLFUND, messageVO.getValue (CollateralVO.COLLFUND));
			trade.setValue (CollateralVO.BROKERID, messageVO.getValue (CollateralVO.BROKERID));
			trade.setValue (CollateralVO.SECURITYTYPE, messageVO.getValue (CollateralVO.SECURITYTYPE));
			if(messageVO.getValue(CollateralVO.COLLATERALTYPECODE)!=null)
				trade.setValue (CollateralVO.COLLATERALTYPECODE, messageVO.getStringValue (CollateralVO.COLLATERALTYPECODE).trim());
			trade.setValue (CollateralVO.COLLATERAL_CREATEDATE, new SysDateObject());
			SLEEventLogger.logDebug (this, "<<End of method createTradeRecord (IValueObject messageVO) >>");
			return trade;
		}
		catch (ValueObjectException ove) {
			SLEEventLogger.logError (this, ove, "Error in Loan Processes");
			throw new LoanException ("Error Creating trade row for new Loan: " + messageVO, ove);
		}
	}
	public IValueObject createActivityRecord (IValueObject newActivity) throws LoanException {
		try {
		    SLEEventLogger.logDebug (this, "<<Start of method createActivityRecord (IValueObject newActivity) >>");
			IValueObject activity = VOFactory.getVO ("CollateralActivity");
			activity.setType("CollateralActivity");
			String areaId = newActivity.getStringValue (CollateralVO.DML_AREAID);
			activity.setValue (CollateralVO.DML_AREAID, areaId);
			String dvpFlag =newActivity.getStringValue (CollateralVO.DVPFLAG);
			SLEEventLogger.logDebug (this, "<<In method createActivityRecord (IValueObject newActivity)  and dvpFlag ::>>"+ dvpFlag);
			if(dvpFlag!=null)
				activity.setValue (CollateralVO.DVPFLAG, dvpFlag);
			activity.setValue (CollateralVO.TRADENUMBER, newActivity.getValue (CollateralVO.TRADENUMBER));
			activity.setValue (CollateralVO.DMLTOE, CollateralVO.DMLTOESUMMARY);
			activity.setValue (CollateralVO.ACTIVITYSTATUS, CollateralState.SETTLEDSTATUS);
			activity.setValue (CollateralVO.COLLATERAL_LASTMODSIGNON, CollateralMessage.COLLSIGNON);
			activity.setValue (CollateralVO.COLLATERAL_LASTMODDATETIME, new SysDateObject());
			activity.setValue (CollateralVO.COLLATERAL_CREATEDATE, new SysDateObject());
			activity.setValue (CollateralVO.ACTIVITYASOFDATE, CalendarUtils.getTodaysDate (areaId));
			SLEEventLogger.logDebug (this, "<<End of method createActivityRecord (IValueObject newActivity) >>");
			return activity;
		}
		catch (ValueObjectException ove) {
			SLEEventLogger.logError (this, ove, "Error in Loan Processes");
			throw new LoanException ("Error Creating new Today Summary for new Loan: " + newActivity, ove);
		}
	}
	public IValueObject createSummaryRecord (IValueObject newActivity, String userid) throws LoanException {
		try {
			SLEEventLogger.logDebug (this, "<<Start of method createSummaryRecord (IValueObject newActivity, String userid) >>");
			// Create a normal activity VO and overwrite relevant values
			IValueObject summary = createActivityRecord (newActivity);
			summary.setValue (CollateralVO.TYPEOFACTIVITY, CollateralVO.TYPEOFACTIVITYSUMMARY);
			summary.setValue (CollateralVO.DMLTOE, CollateralVO.DMLTOESUMMARY);
			summary.setValue (CollateralVO.ACTIVITYID, CollateralVO.SUMMACTIVITYID);
			summary.setValue (CollateralVO.LASTACTIVITYSEQUENCE, CollateralVO.SUMMARYACTIVITYSEQUENCE);
			summary.setValue (CollateralVO.COLLATERAL_LASTMODSIGNON, userid);
			SLEEventLogger.logDebug (this, "<<End of method createSummaryRecord (IValueObject newActivity, String userid) >>");
			return summary;
		}
		catch (ValueObjectException voe) {
			throw new LoanException (voe.getMessage ());
		}
	}
	public void packTradeVO (IValueObject messageVO, IValueObject tradeVO) throws LoanException {
		try {
			SLEEventLogger.logDebug (this, "<<Start of method packTradeVO (IValueObject messageVO, IValueObject tradeVO) >> " );
			tradeVO.setType ("Collateral");
			addIfContains (tradeVO, messageVO, CollateralVO.PRICE);
			addIfContains (tradeVO, messageVO, CollateralVO.QUANTITY);
			addIfContains (tradeVO, messageVO, CollateralVO.AMOUNTDML);
			addIfContains (tradeVO, messageVO, CollateralVO.LASTACTIVITYSEQUENCE);
			addIfContains (tradeVO, messageVO, CollateralVO.LASTACTIVITYTOE);
			addIfContains (tradeVO, messageVO, CollateralVO.TRADESTATUSCODEDML);
			addIfContains (tradeVO, messageVO, CollateralVO.CLOSEDATE);
		}
		catch (ValueObjectException voe) {
			throw new LoanException (voe.getMessage ());
		}
			SLEEventLogger.logDebug (this, "<<End of method packTradeVO (IValueObject messageVO, IValueObject tradeVO) >>");
	}
	public void packActivityVO (IValueObject messageVO, IValueObject activity, BigDecimal activityId) throws LoanException {
		try {
			SLEEventLogger.logDebug (this, "<<start of method packActivityVO >>");
			activity.setType ("CollateralActivity");
			activity.setValue (CollateralVO.TRADENUMBER, messageVO.getValue (CollateralVO.TRADENUMBER));
			activity.setValue (CollateralVO.ACTIVITYID, activityId.abs());
			activity.setValue (CollateralVO.DML_AREAID, messageVO.getValue (CollateralVO.DML_AREAID));
			activity.setValue (CollateralVO.ACTIVITYASOFDATE, messageVO.getValue (CollateralVO.ACTIVITYASOFDATE));
			if(messageVO.getValue (CollateralVO.ACTIVITYTRANDATE) == null){
			SLEEventLogger.logDebug (this, "<<in  method packActivityVO  and activitytrabdate is null ..maybe a UPD ... updating with current date time >>");
			activity.setValue (CollateralVO.ACTIVITYTRANDATE, DateTime.now());
			}else{
			activity.setValue (CollateralVO.ACTIVITYTRANDATE, messageVO.getValue (CollateralVO.ACTIVITYTRANDATE));
			}
			activity.setValue (CollateralVO.CONTRACTUALSETTLEMENTDATE, messageVO.getValue (CollateralVO.ADJUSTEDSETTLEMENTDATE));
			activity.setValue (CollateralVO.ADJUSTEDSETTLEMENTDATE, messageVO.getValue (CollateralVO.ADJUSTEDSETTLEMENTDATE));
			activity.setValue (CollateralVO.DMLTOE, messageVO.getValue (CollateralVO.DMLTOE));
			activity.setValue (CollateralVO.ACTIVITYSTATUS, messageVO.getValue (CollateralVO.ACTIVITYSTATUS));
			activity.setValue (CollateralVO.ACTIVITYQUANTITY, messageVO.getValue (CollateralVO.ACTIVITYQUANTITY));
			activity.setValue (CollateralVO.ACTIVITYAMOUNT, messageVO.getValue (CollateralVO.ACTIVITYAMOUNT));
			activity.setValue (CollateralVO.PRICE, messageVO.getValue (CollateralVO.PRICE));
			activity.setValue (CollateralVO.REFERENCEID, messageVO.getValue (CollateralVO.REFERENCEID));
			activity.setValue (CollateralVO.BROKERID, messageVO.getStringValue (CollateralVO.BROKERID));
			activity.setValue (CollateralVO.COLLCURR, messageVO.getValue (CollateralVO.COLLCURR));
			activity.setValue (CollateralVO.SETLCURR, messageVO.getValue (CollateralVO.SETLCURR));
			activity.setValue (CollateralVO.BUSLINE, messageVO.getValue (CollateralVO.BUSLINE));
			activity.setValue (CollateralVO.CUSIP, messageVO.getValue (CollateralVO.CUSIP));
			activity.setValue (CollateralVO.SECURITYTYPE, messageVO.getValue (CollateralVO.SECURITYTYPE));
			activity.setValue (CollateralVO.POOLID, messageVO.getValue (CollateralVO.POOLID));
			activity.setValue (CollateralVO.SETTLEMENTLOCATION, messageVO.getValue (CollateralVO.SETTLEMENTLOCATION));
			activity.setValue (CollateralVO.TYPEOFACTIVITY, messageVO.getValue (CollateralVO.TYPEOFACTIVITY));
			activity.setValue (CollateralVO.QUANTITYOPENINGTHISACTIVITY, messageVO.getValue (CollateralVO.QUANTITYOPENINGTHISACTIVITY));
			activity.setValue (CollateralVO.QUANTITYCLOSINGTHISACTIVITY, messageVO.getValue (CollateralVO.QUANTITYCLOSINGTHISACTIVITY));
			activity.setValue (CollateralVO.AMOUNTINCREASINGTHISACTIVITY, messageVO.getValue (CollateralVO.AMOUNTINCREASINGTHISACTIVITY));
			activity.setValue (CollateralVO.AMOUNTDECREASINGTHISACTIVITY, messageVO.getValue (CollateralVO.AMOUNTDECREASINGTHISACTIVITY));
			activity.setValue (CollateralVO.NEWPRICETHISACTIVITY, messageVO.getValue (CollateralVO.PRICE));
			activity.setValue (CollateralVO.EFFECTIVELOANQUANTITY, messageVO.getValue (CollateralVO.EFFECTIVELOANQUANTITY));
			activity.setValue (CollateralVO.EFFECTIVELOANAMOUNT, messageVO.getValue (CollateralVO.EFFECTIVELOANAMOUNT));
			activity.setValue (CollateralVO.EFFECTIVEPRICE, messageVO.getValue (CollateralVO.EFFECTIVEPRICE));
			activity.setValue (CollateralVO.PENDINGQUANTITYOPEN, messageVO.getValue (CollateralVO.PENDINGQUANTITYOPEN));
			activity.setValue (CollateralVO.SETTLEDQUANTITYOPEN, messageVO.getValue (CollateralVO.SETTLEDQUANTITYOPEN));
			activity.setValue (CollateralVO.PENDINGQUANTITYCLOSE, messageVO.getValue (CollateralVO.PENDINGQUANTITYCLOSE));
			activity.setValue (CollateralVO.SETTLEDQUANTITYCLOSE, messageVO.getValue (CollateralVO.SETTLEDQUANTITYCLOSE));
			activity.setValue (CollateralVO.PENDINGAMOUNTINCREASE, messageVO.getValue (CollateralVO.PENDINGAMOUNTINCREASE));
			activity.setValue (CollateralVO.SETTLEDAMOUNTINCREASE, messageVO.getValue (CollateralVO.SETTLEDAMOUNTINCREASE));
			activity.setValue (CollateralVO.PENDINGAMOUNTDECREASE, messageVO.getValue (CollateralVO.PENDINGAMOUNTDECREASE));
			activity.setValue (CollateralVO.SETTLEDAMOUNTDECREASE, messageVO.getValue (CollateralVO.SETTLEDAMOUNTDECREASE));
			activity.setValue (CollateralVO.TRADETYPE, messageVO.getValue (CollateralVO.TRADETYPE));
			activity.setValue (CollateralVO.COLLATERAL_CREATEDATE, messageVO.getValue (CollateralVO.COLLATERAL_CREATEDATE));
			activity.setValue (CollateralVO.COLLATERAL_LASTMODSIGNON, messageVO.getValue (CollateralVO.COLLATERAL_LASTMODSIGNON));
			activity.setValue (CollateralVO.COLLATERAL_LASTMODDATETIME, messageVO.getValue (CollateralVO.COLLATERAL_LASTMODDATETIME));
			activity.setValue (CollateralVO.LASTACTIVITYSEQUENCE, messageVO.getValue (CollateralVO.LASTACTIVITYSEQUENCE));
			activity.setValue (CollateralVO.CLOSEDATE, messageVO.getValue (CollateralVO.CLOSEDATE));
			activity.setValue (CollateralVO.COLLFUND, messageVO.getValue (CollateralVO.COLLFUND));
			activity.setValue (CollateralVO.ORIGINATOR, messageVO.getValue (CollateralVO.ORIGINATOR));

			// To compute the activity deliver amount, get the activity amount...
			BigDecimal activityAmount = messageVO.getBigDecimalValue(CollateralVO.ACTIVITYAMOUNT) == null
				? MathUtils.getZeroBD ()
				: messageVO.getBigDecimalValue(CollateralVO.ACTIVITYAMOUNT);

			// ...and then get the rebPrem adjustment amount
			BigDecimal rebPremAmtAdjThisActivity = messageVO.getBigDecimalValue(CollateralVO.REBPREMAMOUNTADJUSTMENTTHISACTIVITY) == null
				? MathUtils.getZeroBD ()
				: messageVO.getBigDecimalValue(CollateralVO.REBPREMAMOUNTADJUSTMENTTHISACTIVITY);

			// Put in the non-zero value, if found.
			BigDecimal amount = (activityAmount.intValue () == 0)
				? rebPremAmtAdjThisActivity
				: activityAmount;

			activity.setValue (CollateralVO.ACTIVITYDELIVERYAMOUNT,amount);
			CalculationHelper.calculateActivity (null, activity);
		}
		catch (ValueObjectException voe) {
			throw new LoanException (voe.getMessage ());
		}
	}
	private void addIfContains (IValueObject newValueObject, IValueObject oldValueObject, String attribute) throws ValueObjectException {
		if (oldValueObject.containsAttribute (attribute)) {
			newValueObject.setValue (attribute, oldValueObject.getValue (attribute));
		}
	}
//	LCM Changes 09Mar2009--> Start
	public IValueObject createCollDMLVO (IValueObject messageVO) throws LoanException {
		SLEEventLogger.logDebug (this, "<<Start of method createTradeRecord (IValueObject messageVO) >> ");
		try {
			
			IValueObject trade = VOFactory.getVO (CollateralVO.COLLATERALDML);
			trade.setType (CollateralVO.COLLATERALDML);
			trade.setValue (CollateralVO.COLLTRADENUMBER, messageVO.getValue (CollateralVO.TRADENUMBER));
			if(messageVO.getValue (CollateralVO.AVAILUPDATEFLAG)!=null)
			trade.setValue (CollateralVO.AVAILUPDATEFLAG, messageVO.getValue (CollateralVO.AVAILUPDATEFLAG));
			if(messageVO.getValue (CollateralVO.BANKFEEAMOUNTACCRUEDDML)!=null)
			trade.setValue (CollateralVO.BANKFEEAMOUNTACCRUEDDML, messageVO.getValue (CollateralVO.BANKFEEAMOUNTACCRUEDDML));
			if(messageVO.getValue (CollateralVO.BANKFEEAMOUNTMTDDML)!=null)
			trade.setValue (CollateralVO.BANKFEEAMOUNTMTDDML, messageVO.getValue (CollateralVO.BANKFEEAMOUNTMTDDML));
			if(messageVO.getValue (CollateralVO.BANKFEEAMOUNTYTDDML)!=null)
			trade.setValue (CollateralVO.BANKFEEAMOUNTYTDDML, messageVO.getValue (CollateralVO.BANKFEEAMOUNTYTDDML));
			if(messageVO.getValue (CollateralVO.BANKFEECHANGEDATE)!=null)
			trade.setValue (CollateralVO.BANKFEECHANGEDATE, messageVO.getValue (CollateralVO.BANKFEECHANGEDATE));
			if(messageVO.getValue (CollateralVO.BANKFEECHANGEDATEDML)!=null)
			trade.setValue (CollateralVO.BANKFEECHANGEDATEDML, messageVO.getValue (CollateralVO.BANKFEECHANGEDATEDML));
			if(messageVO.getValue (CollateralVO.BANKFEEDAILYAMOUNTDML)!=null)
			trade.setValue (CollateralVO.BANKFEEDAILYAMOUNTDML, messageVO.getValue (CollateralVO.BANKFEEDAILYAMOUNTDML));
			if(messageVO.getValue (CollateralVO.LOANNUMBERDML)!=null)
			trade.setValue (CollateralVO.COLLLOANNUMBERDML, messageVO.getValue (CollateralVO.LOANNUMBERDML));
			if(messageVO.getValue (CollateralVO.CREATEDATEDML)!=null)
			trade.setValue (CollateralVO.CREATEDATEDML, messageVO.getValue (CollateralVO.CREATEDATEDML));
			if(messageVO.getValue (CollateralVO.CREATEDATE)!=null)
			trade.setValue (CollateralVO.CREATEDATE, messageVO.getValue (CollateralVO.CREATEDATE));
			if(messageVO.getValue (CollateralVO.CREATESIGNON)!=null)
			trade.setValue (CollateralVO.CREATESIGNON, messageVO.getValue (CollateralVO.CREATESIGNON));
			if(messageVO.getValue (CollateralVO.CREATIONSOURCE)!=null)
			trade.setValue (CollateralVO.CREATIONSOURCE, messageVO.getValue (CollateralVO.CREATIONSOURCE));
			if(messageVO.getValue (CollateralVO.EARNAMOUNTACCRUEDDML)!=null)
			trade.setValue (CollateralVO.EARNAMOUNTACCRUEDDML, messageVO.getValue (CollateralVO.EARNAMOUNTACCRUEDDML));
			if(messageVO.getValue (CollateralVO.EARNAMOUNTMTDDML)!=null)
			trade.setValue (CollateralVO.EARNAMOUNTMTDDML, messageVO.getValue (CollateralVO.EARNAMOUNTMTDDML));			
			if(messageVO.getValue (CollateralVO.EARNAMOUNTYTDDML)!=null)
			trade.setValue (CollateralVO.EARNAMOUNTYTDDML, messageVO.getValue (CollateralVO.EARNAMOUNTYTDDML));
			if(messageVO.getValue (CollateralVO.EARNDAILYAMOUNTDML)!=null)
			trade.setValue (CollateralVO.EARNDAILYAMOUNTDML, messageVO.getValue (CollateralVO.EARNDAILYAMOUNTDML));
			if(messageVO.getValue (CollateralVO.LASTMODDATETIME)!=null)
			trade.setValue (CollateralVO.LASTMODDATETIME, messageVO.getValue (CollateralVO.LASTMODDATETIME));
			if(messageVO.getValue (CollateralVO.LASTMODSIGNON)!=null)
			trade.setValue (CollateralVO.LASTMODSIGNON, messageVO.getValue (CollateralVO.LASTMODSIGNON));
			if(messageVO.getValue (CollateralVO.LOCISSUER)!=null)
			trade.setValue (CollateralVO.LOCISSUER, messageVO.getValue (CollateralVO.LOCISSUER));			
			if(messageVO.getValue (CollateralVO.LOCATIONCODE)!=null)
			trade.setValue (CollateralVO.LOCATIONCODE, messageVO.getValue (CollateralVO.LOCATIONCODE));
			if(messageVO.getValue (CollateralVO.NEGREBAMOUNTACCRUEDDML)!=null)
			trade.setValue (CollateralVO.NEGREBAMOUNTACCRUEDDML, messageVO.getValue (CollateralVO.NEGREBAMOUNTACCRUEDDML));
			if(messageVO.getValue (CollateralVO.NEGREBAMOUNTMTDDML)!=null)
			trade.setValue (CollateralVO.NEGREBAMOUNTMTDDML, messageVO.getValue (CollateralVO.NEGREBAMOUNTMTDDML));
			if(messageVO.getValue (CollateralVO.NEGREBAMOUNTYTDDML)!=null)
			trade.setValue (CollateralVO.NEGREBAMOUNTYTDDML, messageVO.getValue (CollateralVO.NEGREBAMOUNTYTDDML));
			if(messageVO.getValue (CollateralVO.NEGREBCHANGEDATEDML)!=null)
			trade.setValue (CollateralVO.NEGREBCHANGEDATEDML, messageVO.getValue (CollateralVO.NEGREBCHANGEDATEDML));			
			if(messageVO.getValue (CollateralVO.NEGREBDAILYAMOUNTDML)!=null)
			trade.setValue (CollateralVO.NEGREBDAILYAMOUNTDML, messageVO.getValue (CollateralVO.NEGREBDAILYAMOUNTDML));
			if(messageVO.getValue (CollateralVO.PREMPERIODTODATEAMOUNTDML)!=null)
			trade.setValue (CollateralVO.PREMPERIODTODATEAMOUNTDML, messageVO.getValue (CollateralVO.PREMPERIODTODATEAMOUNTDML));
			if(messageVO.getValue (CollateralVO.REBPREMAMOUNTACCRUEDDML)!=null)
			trade.setValue (CollateralVO.REBPREMAMOUNTACCRUEDDML, messageVO.getValue (CollateralVO.REBPREMAMOUNTACCRUEDDML));
			if(messageVO.getValue (CollateralVO.REBPREMAMOUNTMTDDML)!=null)
			trade.setValue (CollateralVO.REBPREMAMOUNTMTDDML, messageVO.getValue (CollateralVO.REBPREMAMOUNTMTDDML));
			if(messageVO.getValue (CollateralVO.REBPREMAMOUTYTDDML)!=null)
			trade.setValue (CollateralVO.REBPREMAMOUTYTDDML, messageVO.getValue (CollateralVO.REBPREMAMOUTYTDDML));
			if(messageVO.getValue (CollateralVO.REBPREMCHANGEDATEDML)!=null)
			trade.setValue (CollateralVO.REBPREMCHANGEDATEDML, messageVO.getValue (CollateralVO.REBPREMCHANGEDATEDML));
			if(messageVO.getValue (CollateralVO.REBPREMDAILYAMOUNTDML)!=null)
			trade.setValue (CollateralVO.REBPREMDAILYAMOUNTDML, messageVO.getValue (CollateralVO.REBPREMDAILYAMOUNTDML));
			if(messageVO.getValue (CollateralVO.REDENOMFLAG)!=null)
			trade.setValue (CollateralVO.REDENOMFLAG, messageVO.getValue (CollateralVO.REDENOMFLAG));
			if(messageVO.getValue (CollateralVO.REMARKSDML)!=null)
			trade.setValue (CollateralVO.REMARKSDML, messageVO.getValue (CollateralVO.REMARKSDML));
			if(messageVO.getValue (CollateralVO.RESERVEFORFLAG)!=null)
			trade.setValue (CollateralVO.RESERVEFORFLAG, messageVO.getValue (CollateralVO.RESERVEFORFLAG));			
			if(messageVO.getValue (CollateralVO.TICKETPRINTFLAG)!=null)
			trade.setValue (CollateralVO.TICKETPRINTFLAG, messageVO.getValue (CollateralVO.TICKETPRINTFLAG));			
			if(messageVO.getValue (CollateralVO.TRADESTATUSCODEDML)!=null)
			trade.setValue (CollateralVO.TRADESTATUSCODEDML, messageVO.getValue (CollateralVO.TRADESTATUSCODEDML));
			
			SLEEventLogger.logDebug (this, "<<End of method createCollDMLVO (IValueObject messageVO) >>");
			return trade;
		}
		catch (ValueObjectException ove) {
			SLEEventLogger.logError (this, ove, "Error in Collateral Processes");
			throw new LoanException ("Error Creating collateralDML row for new Coll: " + messageVO, ove);
		}
	}
	public IValueObject createCollActivityDMLVO (IValueObject messageVO,IValueObject activity,BigDecimal activityId) throws LoanException {
		SLEEventLogger.logDebug (this, "<<Start of method createTradeRecord (IValueObject messageVO) >> ");
		try {
			
			IValueObject trade = VOFactory.getVO (CollateralVO.COLLACTIVITYDML);
			trade.setType (CollateralVO.COLLACTIVITYDML);

			trade.setValue (CollateralVO.COLLTRADENUMBER, activity.getValue (CollateralVO.TRADENUMBER));
			trade.setValue (CollateralVO.ACTIVITYID, activityId.abs());	
			trade.setValue (CollateralVO.TYPEOFACTIVITY, messageVO.getValue (CollateralVO.TYPEOFACTIVITY));
			if(messageVO.getValue (CollateralVO.REBPREMAMOUNTACCRUEDDML)!=null)
				trade.setValue (CollateralVO.REBPREMAMOUNTACCRUEDDML, messageVO.getValue (CollateralVO.REBPREMAMOUNTACCRUEDDML));
			if(messageVO.getValue (CollateralVO.NEGREBAMOUNTACCRUEDDML)!=null)
				trade.setValue (CollateralVO.NEGREBAMOUNTACCRUEDDML, messageVO.getValue (CollateralVO.NEGREBAMOUNTACCRUEDDML));
			trade.setValue (CollateralVO.LASTACTIVITYSEQUENCE, messageVO.getValue (CollateralVO.LASTACTIVITYSEQUENCE));
			if(messageVO.getValue (CollateralVO.ORIGINATOR)!=null)
				trade.setValue (CollateralVO.ORIGINATOR, messageVO.getValue (CollateralVO.ORIGINATOR));
			
			if(messageVO.getValue (CollateralVO.DMLRETURNCODE)!=null)
				trade.setValue (CollateralVO.DMLRETURNCODE, messageVO.getValue (CollateralVO.DMLRETURNCODE));
			if(messageVO.getValue (CollateralVO.DMLREASONCODE)!=null)
				trade.setValue (CollateralVO.DMLREASONCODE, messageVO.getValue (CollateralVO.DMLREASONCODE));
			if(messageVO.getValue (CollateralVO.DMLUPDATESTATUS)!=null)
				trade.setValue (CollateralVO.DMLUPDATESTATUS, messageVO.getValue (CollateralVO.DMLUPDATESTATUS));		
			if(messageVO.getValue (CollateralVO.CREATESIGNON)!=null)
				trade.setValue (CollateralVO.CREATESIGNON, activity.getValue (CollateralVO.CREATESIGNON));
			if(messageVO.getValue (CollateralVO.LASTMODDATETIME)!=null)
				trade.setValue (CollateralVO.LASTMODDATETIME, activity.getValue (CollateralVO.LASTMODDATETIME));
			if(messageVO.getValue (CollateralVO.LASTMODSIGNON)!=null)
					trade.setValue (CollateralVO.LASTMODSIGNON, activity.getValue (CollateralVO.COLLATERAL_LASTMODSIGNON));
			if(messageVO.getValue (CollateralVO.CREATEDATE)!=null)
					trade.setValue (CollateralVO.CREATEDATE, activity.getValue (CollateralVO.CREATEDATE));
			
			SLEEventLogger.logDebug (this, "<<End of method createCollActivityDMLVO (IValueObject messageVO) >>");
			return trade;
		}
		catch (ValueObjectException ove) {
			SLEEventLogger.logError (this, ove, "Error in Collateral Processes");
			throw new LoanException ("Error Creating collateralDML row for new Coll: " + messageVO, ove);
		}
	}	
	
//	LCM Changes 09Mar2009--> End
}
