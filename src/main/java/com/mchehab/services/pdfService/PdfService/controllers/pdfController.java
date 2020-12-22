package com.mchehab.services.pdfService.PdfService.controllers;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Map;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mchehab.services.pdfService.PdfService.chrome.ChromeContext;
import com.mchehab.services.pdfService.PdfService.common.DirectoryHelper;
import com.mchehab.services.pdfService.PdfService.common.Settings;
import com.mchehab.services.pdfService.PdfService.json.JSONObject;
import com.mchehab.services.pdfService.PdfService.models.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController

public class pdfController {

    private  Logger _logger = LoggerFactory.getLogger(pdfController.class);
  
    @Autowired
    private Settings _settings;

    @RequestMapping("/ping")
    public Response ping() {
        return new Response(true, String.format("%s", Calendar.getInstance().getTime()));
    }

    @RequestMapping("/pdf/create")
    public Response generate(@RequestParam Map<String, Object> params) {

        String response = "";
        String template = "";

        if (!params.containsKey("template")) {
            return new Response(false, "required [template] parameter is missing");
        }

        try {

            String requestUrl = this._settings.getRepository() + "/" + params.get("template") + ".html";

            GetRequest getRequest  = Unirest.get(requestUrl);

            //check if github requires authorization
            if(!StringUtils.isEmpty(this._settings.getAuthorizationToken())){
                getRequest = getRequest.header("Authorization", "bearer " + this._settings.getAuthorizationToken());
            }
            
            HttpResponse<String> html = getRequest.asString();

            template = html.getBody().toString();

            StringTemplate stringTemplate = new StringTemplate(template);
   
            params.keySet().forEach((param)->{
                if(!param.equals("template")){
                    Object value = params.get(param);    
                    
                    if(param.equals("data")){
                        JSONObject data = new JSONObject(value.toString());
                        stringTemplate.setAttribute(param, data);
                    }
                    else
                         stringTemplate.setAttribute(param, value);
                }
            });

            String guid = java.util.UUID.randomUUID().toString();
            String templatesDirectory = "html-templates";
            String documentsDirectory = "webapp/documents";
    
            DirectoryHelper.createIfNotExists(templatesDirectory);
            DirectoryHelper.createIfNotExists(documentsDirectory);

            _logger.info("Creating files with name : {}", guid);
    
            //prepare the html file
            String createdAt = DirectoryHelper.createFromString(templatesDirectory,  String.format("%s.html", guid) , stringTemplate.toString());

            _logger.info("Created the html file at {}", createdAt);
            ChromeContext context = new ChromeContext();
            String pdf = DirectoryHelper.joinFromRoot(documentsDirectory) + String.format("/%s.pdf", guid);
            context.generatePDF(Paths.get(pdf),  DirectoryHelper.getLocalUrl(templatesDirectory + String.format("/%s.html", guid)), _settings.getChromePath());
            response = DirectoryHelper.toUrl("documents", String.format("%s.pdf", guid), _settings);
        } 
        catch (FileNotFoundException e) {  
            _logger.error("FileNotFoundException: Failed to generate the pdf document", e);            
            return new Response(e);
        } catch (UnsupportedEncodingException e) {    
            _logger.error("UnsupportedEncodingException: Failed to generate the pdf document", e);            
            return new Response(e);
        }
        catch (Exception e) {
            _logger.error("Exception: Failed to generate the pdf document", e);            
            return new Response(e);
        } 

        return new Response(true, response);
    }
}