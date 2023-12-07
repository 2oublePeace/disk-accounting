package com.emiryanvl.reportservice.service

import org.springframework.http.ResponseEntity
import java.io.File

interface CsvService {
    fun convertJsonToCsv(jsonString: String): File
    fun uploadCsvFile(csvFile: File): ResponseEntity<String>
}