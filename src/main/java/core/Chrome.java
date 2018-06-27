package core;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern; 

public class Chrome {
	
static WebDriver driver;
	
	public static void main(String[] args) throws InterruptedException {

		// cleaning up the logs of the output of the console
		Logger logger = Logger.getLogger("");
		logger.setLevel(Level.OFF);
		
		String driverPath = "";
		
		String[] url_array = { 
				"http://alex.academy/exe/payment_tax/index.html",
				"http://alex.academy/exe/payment_tax/index2.html",
			    "http://alex.academy/exe/payment_tax/index3.html", 
				"http://alex.academy/exe/payment_tax/index4.html", 
				"http://alex.academy/exe/payment_tax/index5.html",
				"http://alex.academy/exe/payment_tax/indexE.html"
		};
		
		if (System.getProperty("os.name").toUpperCase().contains("MAC"))
			driverPath = "./resources/webdrivers/mac/chromedriver";
		else if (System.getProperty("os.name").toUpperCase().contains("WINDOWS"))
			driverPath = "./resources/webdrivers/pc/chromedriver.exe";
		else
			throw new IllegalArgumentException("Unknown OS");
		
		System.setProperty("webdriver.chrome.driver", driverPath);

		// cleaning up the logs of the output of the console
		System.setProperty("webdriver.chrome.silentOutput", "true");

		ChromeOptions options = new ChromeOptions();
		options.addArguments("disable-infobars");
		options.addArguments("--disable-notifications");

		if (System.getProperty("os.name").toUpperCase().contains("MAC"))
			options.addArguments("-start-fullscreen");
		else if (System.getProperty("os.name").toUpperCase().contains("WINDOWS"))
			options.addArguments("--start-maximized");
		else
			throw new IllegalArgumentException("Unknown OS");

		driver = new ChromeDriver(options);
		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		
		for (String url: url_array) {
		
		driver.get(url);
		
		String string_monthly_payment_and_tax = driver.findElement(By.id("id_monthly_payment_and_tax")).getText();
		String regex = "^" // ^ Start of line
				+ "(?:.*?)?" 
				+ "(?:\\$*)?" 
				+ "(?:\\s*)?" 
				+ "((?:\\d*)|(?:\\d*)(?:\\.)(?:\\d*))" 
				+ "(?:\\s*)?"
				+ "(?:[/]*|,\\s*[A-Z]*[a-z]*\\s*[:]*)?" 
				+ "(?:\\s*)?" 
				+ "((?:\\d*)|(?:\\d*)(?:\\.)(?:\\d*))"
				+ "(?:\\s*)?" 
				+ "(?:%)?" 
				+ "(?:\\s*)?" 
				+ "$"; // $ End of line
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(string_monthly_payment_and_tax);
		m.find();
		
		double monthly_payment = Double.parseDouble(m.group(1));
		double tax = Double.parseDouble(m.group(2));
		
		// (91.21 * 8.25) / 100 = 7.524825 rounded => 7.52
		double monthly_and_tax_amount = new BigDecimal((monthly_payment * tax) / 100).setScale(2, RoundingMode.HALF_UP)
				.doubleValue();
		
		// 91.21 + 7.52 = 98.734825 rounded => 98.73
		double monthly_payment_with_tax = new BigDecimal(monthly_payment + monthly_and_tax_amount)
				.setScale(2, RoundingMode.HALF_UP).doubleValue();
		
		// double annual_payment_with_tax = monthly_payment_with_tax * 12;
		double annual_payment_with_tax = new BigDecimal(monthly_payment_with_tax * 12).setScale(2, RoundingMode.HALF_UP)
				.doubleValue();
		
		driver.findElement(By.id("id_annual_payment_with_tax")).sendKeys(String.valueOf(annual_payment_with_tax));
		driver.findElement(By.id("id_validate_button")).submit();
//		driver.findElement(By.xpath("//input[@id='id_validate_button']")).click();		
		String actual_result = driver.findElement(By.id("id_result")).getText();
		
		System.out.println("String: \t\t" + string_monthly_payment_and_tax);
		System.out.println("Annual Payment with Tax: " + annual_payment_with_tax);
		System.out.println("Result: \t\t" + actual_result);
		
		}
		
		driver.quit();
	}
	}