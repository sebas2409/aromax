package org.example.notino;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.WheelInput;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Main {


    public static void main(String[] args) throws IOException {
        initialInfo();
        getNotes();
    }

    public static void getNotes() throws IOException {
        //Obtener los perfumes del archivo JSON
        var mapper = new ObjectMapper();
        var file = new File("src/main/resources/notino.json");
        var perfumes = List.of(mapper.readValue(file, NotinoPerfume[].class));


        perfumes.forEach(perfume -> {
            // Crear un hilo para cada perfume y obtener las notas
            var thread = new Thread(() -> {
                try {
                    var cookiesButton = By.xpath("//*[@id=\"onetrust-accept-btn-handler\"]");
                    var imgPath = By.xpath("//*[@id=\"pdImageGallery\"]/div[2]/div[2]/div[1]/div/div/img");
                    var expandButton = By.xpath("//*[@id=\"tabAnchor\"]/div[2]/button");
                    var tableRowPath = By.xpath("//*[@id=\"pd-description-wrapper\"]/div[1]/table/tbody/tr");
                    WebDriver driver = new ChromeDriver();
                    var wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                    driver.get(perfume.getLink());
                    var cookiesButtonElement = wait.until(ExpectedConditions.elementToBeClickable(cookiesButton));
                    cookiesButtonElement.click();
                    new Actions(driver)
                            .scrollToElement(driver.findElement(expandButton))
                            .scrollFromOrigin(WheelInput.ScrollOrigin.fromElement(driver.findElement(expandButton)), 0, 500)
                            .perform();
                    Thread.sleep(2000);
                    var expandButtonElement = wait.until(ExpectedConditions.elementToBeClickable(expandButton));
                    expandButtonElement.click();
                    try {
                        var img = wait.until(ExpectedConditions.presenceOfElementLocated(imgPath));
                        var src = img.getAttribute("src");
                        System.out.println(src);
                        perfume.setImageSrc(src);
                        var tableRows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRowPath));
                        if (tableRows.size() == 4) {
                            for (int i = 0; i < tableRows.size(); i++) {
                                var tds = tableRows.get(i).findElements(By.tagName("td"));
                                var value = tds.get(1).getText();
                                switch (i) {
                                    case 0 -> perfume.setHeadNotes(List.of(value.split(",")));
                                    case 1 -> perfume.setHeartNotes(List.of(value.split(",")));
                                    case 2 -> perfume.setBaseNotes(List.of(value.split(",")));
                                    case 3 -> perfume.setFraganceType(List.of(value.split(",")));
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    driver.quit();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                mapper.writeValue(new File("src/main/resources/final.json"), perfumes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void initialInfo() throws IOException {
        // Los xpath son las rutas de los elementos en el DOM
        var gripPath = By.xpath("//*[@id=\"productListWrapper\"]/div[2]/div");
        var cookiesButton = By.xpath("//*[@id=\"onetrust-accept-btn-handler\"]");

        var perfumes = new ArrayList<NotinoPerfume>();
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.notino.es/perfumes/?npc=327");
        var driverWait = new WebDriverWait(driver, Duration.ofSeconds(10));

        //Hacer click en el botón de aceptar cookies una vez que se haya cargado
        var cookiesButtonElement = driverWait.until(ExpectedConditions.elementToBeClickable(cookiesButton));
        cookiesButtonElement.click();

        // Esperar a que se carguen los elementos del grid de perfumes
        var elements = driverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(gripPath));

        elements.forEach(element -> {
            try {
                // Hacer scroll hasta el elemento
                new Actions(driver)
                        .scrollToElement(element)
                        .scrollFromOrigin(WheelInput.ScrollOrigin.fromElement(element), 0, 200)
                        .perform();
                //Obtener los elementos del perfume
                var link = element.findElement(By.tagName("a")).getAttribute("href");
                var brand = element.findElement(By.tagName("h2")).getText();
                var name = element.findElement(By.tagName("h3")).getText();
                var description = element.findElement(By.tagName("p")).getText();
                var spans = element.findElements(By.tagName("span"));
                List<String> stringStream = spans.stream().map(WebElement::getText).filter(string -> string.contains(",")).toList();
                var price = Double.parseDouble(stringStream.get(0).replace(",", "."));
                System.out.println(name);
                var perfume = new NotinoPerfume(brand, name, description, link, price);
                perfumes.add(perfume);
                System.out.println(perfume);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        System.out.println("Perfumes: " + perfumes.size());

        // Escribir los perfumes en un archivo JSON
        var mapper = new ObjectMapper();
        var file = new File("src/main/resources/notino.json");
        mapper.writeValue(file, perfumes);
    }
}
