package com.emiryanvl.reportservice.controller

import com.emiryanvl.reportservice.config.ReportProperties
import com.emiryanvl.reportservice.service.CsvService
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.amqp.core.Queue

@RestController
@RequestMapping("/report")
class ReportController(
        val reportProperties: ReportProperties,
        val csvService: CsvService,
        private val rabbitTemplate: RabbitTemplate,
        private val reportQueue: Queue
) {
    @GetMapping
    fun getReport() {
        val disks = RestTemplate().getForObject(reportProperties.reportUrl, String::class.java).toString()
        rabbitTemplate.convertAndSend(reportQueue.name, csvService.convertJsonToCsv(disks))
    }
}