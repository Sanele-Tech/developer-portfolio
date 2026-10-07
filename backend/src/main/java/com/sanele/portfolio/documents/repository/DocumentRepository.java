package com.sanele.portfolio.documents.repository;

import com.sanele.portfolio.documents.entity.Document;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByDocumentTypeOrderByCreatedAtDesc(String documentType);
    Optional<Document> findFirstByDocumentTypeIgnoreCaseOrderByCreatedAtDescIdDesc(String documentType);
    List<Document> findAllByOrderByCreatedAtDescIdDesc();
}