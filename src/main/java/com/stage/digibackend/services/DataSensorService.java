package com.stage.digibackend.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.AreaBreakType;
import com.stage.digibackend.Collections.Device;
import com.stage.digibackend.Collections.Historique;
import com.stage.digibackend.Collections.Sensor;
import com.stage.digibackend.Collections.DataSensor;
import com.stage.digibackend.Enumeration.GrowthStatus;
import com.stage.digibackend.repository.DeviceRepository;
import com.stage.digibackend.repository.DataSensorRepository;
import com.stage.digibackend.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

@Service
public class DataSensorService implements IDataSensorService {

    @Autowired
    DeviceRepository deviceRepository ;
    @Autowired
    SensorRepository sensorRepository ;
    @Autowired
    DataSensorRepository dataSensorRepository ;
    @Autowired
    IhistoriqueService ihistoriqueService ;


    @Override
    public List<DataSensor> findAllDataSensors() {
        return dataSensorRepository.findAll();
    }

    @Override
    public DataSensor findDataSensorById(String idDataSensor) {
        return dataSensorRepository.findById(idDataSensor)
                .orElseThrow(() -> new IllegalArgumentException("Data sensor not found"));
    }

    @Override
    public void deleteDataSensorById(String idDataSensor) {
        dataSensorRepository.deleteById(idDataSensor);
    }

    @Override
    public DataSensor affecteSensorDevice(String idSensor, String idDevice) {
        Device device = deviceRepository.findById(idDevice).get();
        Sensor sensor = sensorRepository.findById(idSensor).get();

        DataSensor dataSensor = new DataSensor();
        dataSensor.setSensor(sensor);
        dataSensor.setDevice(device);

         return dataSensorRepository.save(dataSensor) ;
    }

    @Override
    public DataSensor loadDataInSensorDevice(String idSensor, String idDevice, Double data, GrowthStatus growthStatus , LocalDateTime latestUpdate) {
        String action = "";

        Device device = deviceRepository.findById(idDevice)
                .orElseThrow(() -> new NoSuchElementException("Device not found with ID: " + idDevice));

        Sensor sensor = sensorRepository.findById(idSensor)
                .orElseThrow(() -> new NoSuchElementException("Sensor not found with ID: " + idSensor));


        DataSensor dataSensor = dataSensorRepository.findDataSensorByDeviceAndSensor(device, sensor);
        if (dataSensor.getLatestUpdate() != null && !latestUpdate.toLocalDate().isEqual(dataSensor.getLatestUpdate().toLocalDate())) {
            dataSensor.setTotal(0.0);
            dataSensor.setData(0.0);
        }
        dataSensor.setData(data);
        //LocalDateTime.now()
        dataSensor.setLatestUpdate(latestUpdate);
        dataSensor.setGrowthStatus(growthStatus);

        if(sensor.getIsPulse()){
            growthStatus = GrowthStatus.POSITIVE ;
            data = sensor.getPulseValue() ;
            if(sensor.getPulseInit() != null ||sensor.getPulseInit()  >= 0 )
            sensor.setPulseInit(sensor.getPulseInit() + data);
            else
                sensor.setPulseInit(data);

            sensorRepository.save(sensor) ;
        }
        switch (growthStatus) {
            case POSITIVE:
                if (dataSensor.getTotal() != null) {
                    dataSensor.setTotal(dataSensor.getTotal() + data);
                } else {
                    dataSensor.setTotal(data);
                }
                action = "increase in value";
                break;
            case NEGATIVE:
                if (dataSensor.getTotal() != null) {
                    dataSensor.setTotal(dataSensor.getTotal() - data);
                } else {
                    dataSensor.setTotal(-data);
                }
                action = "decrease in value";
                break;
            case NEUTRAL:
                action = "stagnation in value";
                break;
        }

        dataSensorRepository.save(dataSensor);

        Historique historique = new Historique();
        historique.setAction(action);
        historique.setDate(LocalDateTime.now());
        historique.setDataSensor(dataSensor);

        ihistoriqueService.addHistorique(historique);

        return dataSensor;
    }

    @Override
    public byte[] generateDataSensorHistoriquePdf(String dataSensorId) throws IOException {
            // Get the data sensor and its historique data
            DataSensor dataSensor = dataSensorRepository.findById(dataSensorId)
                    .orElseThrow(() -> new IllegalArgumentException("Data sensor not found"));

            List<Historique> historiqueList = ihistoriqueService.findHistoriqueByDeviceAndSensor(
                    dataSensor.getDevice().getDeviceId(),
                    dataSensor.getSensor().getSensorId());

            // Get the device and its sensors
            Device device = dataSensor.getDevice();
            Sensor sensor = dataSensor.getSensor();

            // Create a new PDF document
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(byteArrayOutputStream);
            PdfDocument pdf = new PdfDocument(writer);

            // Create a new page
            Document document = new Document(pdf);

            // Page content
            document.add(new Paragraph("Historique for Data Sensor: " + dataSensorId).setBold());
            document.add(new Paragraph("\n"));

            // Add device information
            document.add(new Paragraph("Device Information:").setBold());
            document.add(new Paragraph("Device ID: " + device.getDeviceId()));
            document.add(new Paragraph("MAC Address: " + device.getMacAdress()));
            document.add(new Paragraph("Name: " + device.getNom()));
            document.add(new Paragraph("Description: " + device.getDescription()));
            document.add(new Paragraph("Latitude: " + device.getLat()));
            document.add(new Paragraph("Longitude: " + device.getLng()));
            document.add(new Paragraph("\n"));

            // Add sensor information
            document.add(new Paragraph("Sensor Information:").setBold());
            document.add(new Paragraph("Sensor ID: " + sensor.getSensorId()));
            document.add(new Paragraph("Sensor Name: " + sensor.getSensorName()));
            document.add(new Paragraph("Range Min: " + sensor.getRangeMin()));
            document.add(new Paragraph("Range Max: " + sensor.getRangeMax()));
            document.add(new Paragraph("Unit: " + sensor.getUnit()));
            document.add(new Paragraph("Unit Symbol: " + sensor.getSymboleUnite()));
            document.add(new Paragraph("Signal: " + sensor.getSignal()));
            document.add(new Paragraph("Coefficient a: " + sensor.getA()));
            document.add(new Paragraph("Coefficient b: " + sensor.getB()));
            document.add(new Paragraph("\n"));

            // Add historique data
            document.add(new Paragraph("Historique Data:").setBold());
            for (Historique historique : historiqueList) {
                float remainingHeight = document.getPdfDocument().getDefaultPageSize().getHeight() - document.getRenderer().getCurrentArea().getBBox().getY();
                if (remainingHeight < 50) {
                    // Create a new page if remaining space is not sufficient
                    document.add(new AreaBreak());
                }

                document.add(new Paragraph("Date: " + historique.getDate()).setBold());
                document.add(new Paragraph("Action: " + historique.getAction()));

                // Add more data sensor information as needed
                document.add(new Paragraph("Latest Update: " + historique.getDataSensor().getLatestUpdate()));
                document.add(new Paragraph("Growth Status: " + historique.getDataSensor().getGrowthStatus()));
                document.add(new Paragraph("Data: " + historique.getDataSensor().getData()));
                document.add(new Paragraph("Total: " + historique.getDataSensor().getTotal()));

                document.add(new Paragraph("\n"));
            }

            document.close();

            return byteArrayOutputStream.toByteArray();
        }






    @Override
    public String findByTwoId(String idSensor, String idDevice) {
        Optional<Device> device= deviceRepository.findById(idDevice);
        Sensor sensor = sensorRepository.findSensorBySensorId(idSensor);
        DataSensor dataSensor = dataSensorRepository.findDataSensorByDeviceAndSensor(device, sensor);
        return dataSensor.getDataSensorId();
    }

}



