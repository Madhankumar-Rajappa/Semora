package com.campustrack.service;

import com.campustrack.dao.RecordDAO;
import com.campustrack.model.DocumentRecord;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing student certificates, result sheets, and lab records.
 */
public class RecordService {

    private final RecordDAO recordDAO = new RecordDAO();

    public List<DocumentRecord> getRecords(int studentId, Integer semesterId, String category, String search) throws SQLException {
        return recordDAO.findAll(studentId, semesterId, category, search);
    }

    public DocumentRecord getRecord(int id, int studentId) throws SQLException {
        return recordDAO.findByIdAndStudentId(id, studentId);
    }

    public DocumentRecord createRecord(DocumentRecord record) throws SQLException {
        int id = recordDAO.create(record);
        record.setId(id);
        return record;
    }

    public boolean updateRecord(DocumentRecord record) throws SQLException {
        return recordDAO.update(record);
    }

    public boolean deleteRecord(int id, int studentId) throws SQLException {
        return recordDAO.delete(id, studentId);
    }
}
