package kz.salyqtez.broker.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import kz.salyqtez.broker.helper.ExcelHelper;
import kz.salyqtez.broker.model.Tutorial;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import kz.salyqtez.broker.repository.TutorialRepository;

@Service
public class ExcelService {
  @Autowired
  TutorialRepository repository;

  public void save(MultipartFile file) {
    try {
      List<Tutorial> tutorials = ExcelHelper.excelToTutorials(file.getInputStream());
      repository.saveAll(tutorials);
    } catch (IOException e) {
      throw new RuntimeException("fail to store excel data: " + e.getMessage());
    }
  }

  public ByteArrayInputStream load() {
    List<Tutorial> tutorials = repository.findAll();

    ByteArrayInputStream in = ExcelHelper.tutorialsToExcel(tutorials);
    return in;
  }

  public List<Tutorial> getAllTutorials() {
    return repository.findAll();
  }
}
