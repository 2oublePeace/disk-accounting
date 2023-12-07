package com.emiryanvl.reportservice.service.impl

import com.emiryanvl.reportservice.config.ReportProperties
import com.emiryanvl.reportservice.dto.DiskDto
import com.emiryanvl.reportservice.service.CsvService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.opencsv.CSVWriter
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.core.io.FileSystemResource
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.io.File
import java.io.FileWriter
import java.net.URI
import java.nio.file.Paths

@Service
class CsvServiceImpl(val reportProperties: ReportProperties) : CsvService {
    override fun convertJsonToCsv(jsonString: String): File {
        val objectMapper = jacksonObjectMapper()
        val disks: List<DiskDto> = objectMapper.readValue(jsonString)

        val csvFile = File.createTempFile("disk-report", ".csv")
        CSVWriter(FileWriter(csvFile)).use { csvWriter ->
            val headerRecord = arrayOf("Title", "Is rented")
            csvWriter.writeNext(headerRecord)

            for (disk in disks) {
                val record = arrayOf(disk.title, disk.isRented.toString())
                csvWriter.writeNext(record)
            }
        }

        return csvFile
    }

    @RabbitListener(queues = ["\${reportQueue.name}"])
    override fun uploadCsvFile(csvFile: File): ResponseEntity<String> {
        val restTemplate = RestTemplate()
        val fileResource = FileSystemResource(Paths.get(csvFile.absolutePath))

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val parts: MultiValueMap<String, Any> = LinkedMultiValueMap()
        parts.add("file", fileResource)

        return restTemplate.exchange(
                RequestEntity(
                    parts,
                    headers,
                    HttpMethod.POST,
                    URI.create(reportProperties.reportUrl)
                ),
                String::class.java
        )
    }
}