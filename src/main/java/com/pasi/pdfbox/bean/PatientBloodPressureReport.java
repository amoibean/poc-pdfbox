package com.pasi.pdfbox.bean;

import java.util.Date;
import java.util.List;

/**
 * Created by bean on 9/28/16.
 */
public class PatientBloodPressureReport {
    private String patientName;
    private Date reportDate;
    private int duration; // 3, 6, 9 months
    private Date fromDate;
    private Date toDate;
    private int desiredSystolic;
    private int desiredDiastolic;
    private List<BloodPressureRecord> bloodPressureRecords;
    private String comments;

    public int getResult() {
        // TODO: implement the result logic
        return 0; // in range
    }

    public BloodPressureRecord getAverageBloodPressure() {
        BloodPressureRecord average = new BloodPressureRecord();
        int count = bloodPressureRecords.size();
        int sumSystolic = 0, sumDiastolic = 0, sumPulse = 0;
        for (BloodPressureRecord record : bloodPressureRecords) {
            sumSystolic += record.getSystolic();
            sumDiastolic += record.getDiastolic();
            sumPulse += record.getPulse();
        }
        average.setSystolic(sumSystolic / count);
        average.setDiastolic(sumDiastolic / count);
        average.setPulse(sumPulse / count);
        return average;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public int getDesiredSystolic() {
        return desiredSystolic;
    }

    public void setDesiredSystolic(int desiredSystolic) {
        this.desiredSystolic = desiredSystolic;
    }

    public int getDesiredDiastolic() {
        return desiredDiastolic;
    }

    public void setDesiredDiastolic(int desiredDiastolic) {
        this.desiredDiastolic = desiredDiastolic;
    }

    public List<BloodPressureRecord> getBloodPressureRecords() {
        return bloodPressureRecords;
    }

    public void setBloodPressureRecords(List<BloodPressureRecord> bloodPressureRecords) {
        this.bloodPressureRecords = bloodPressureRecords;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }


}
