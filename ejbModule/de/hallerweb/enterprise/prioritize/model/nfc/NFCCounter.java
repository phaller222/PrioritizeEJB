package de.hallerweb.enterprise.prioritize.model.nfc;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.apache.commons.lang3.StringUtils;

@Entity
public class NFCCounter extends PCounter {
	
	@OneToOne
	NFCUnit nfcUnit;
	
	String uuid;
	
	public NFCUnit getNfcUnit() {
		return nfcUnit;
	}

	public void setNfcUnit(NFCUnit nfcUnit) {
		this.nfcUnit = nfcUnit;
	}

	public int getId() {
		return id;
	}

	@Override
	public long getValue() {
		String payload = nfcUnit.getPayload();
		if (payload != null && StringUtils.isNumeric(payload)) {
			return Long.parseLong(payload);
		} else {
			return -1;
		}
	}

	@Override
	public void setValue(long value) {
		nfcUnit.setPayload(String.valueOf(value));
	}

	@Override
	public void incCounter() {
		String payload = nfcUnit.getPayload();
		if (payload != null && StringUtils.isNumeric(payload)) {
			long newValue = Long.parseLong(payload) + 1;
			System.out.println("SET: " + newValue);
			nfcUnit.setPayload(String.valueOf(newValue));
		}
	}

	@Override
	public void decCounter() {
		String payload = nfcUnit.getPayload();
		if (payload != null && StringUtils.isNumeric(payload)) {
			long newValue = Long.parseLong(payload) - 1;
			nfcUnit.setPayload(String.valueOf(newValue));
		}
	}
	
	@Override
	public String getUuid() {
		return nfcUnit.getUuid();
	}

}
