package com.numpy.scraping.team17;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.numpy.scraping.team17.model.Recipe;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * 
 * Generic implementation to extract recipes based on morbidity
 * 
 * @author vbodepu
 *
 */
public class BaseRecipeExtractor {

	private static final String HOME_PAGE = "https://www.tarladalal.com/";
	private static final int MAX_RECEIPES_PER_MORBIDITY = 2;
	private static final int MAX_PAGES_TO_FETCH = 1;
	WebDriver driver = null;

	public BaseRecipeExtractor() {
		super();
	}

	@BeforeClass
	protected void loadHomePage() {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
		driver.get(HOME_PAGE);
		driver.manage().window().maximize();
	}
	
	@AfterClass
	protected void closeBrower() {
		driver.quit();
	}

	/**
	 * @param links
	 * @param allRecipes
	 */
	protected void exportRecipes(String morbidity) {

		// arraylist to store all the links
		ArrayList<String> links = new ArrayList<>(14);

		fetchAllRecipeUrls(links, driver);
		System.out.println("Size of links Urls: " + links.size());

		// load each recipe page from above list and populate the recipe object
		// accordingly
		List<Recipe> allRecipes = new ArrayList<Recipe>();

		for (String recipePageUrl : links) {
			Recipe recipe = new Recipe();
			recipe.setTargettedMorbidConditions(morbidity);
			recipe.setRecipeURL(recipePageUrl);

			// populate all other recipe fields from the details page.
			driver.get(recipePageUrl);
			System.out.println("loaded page " + recipePageUrl);

			// read xpaths and values for name, ingredients etc
			populateRecipe(recipe);
			System.out.println(recipe);

			allRecipes.add(recipe);
		}
		
		// TODO : Export allRecipes into excel Morbidity_All.xlsx . e.g., Hypothyroidism_All.xlsx
		
		
		
	}

	private void populateRecipe(Recipe recipe) {

		// Recipe Id
		String tempRecipeId = recipe.getRecipeURL().substring(recipe.getRecipeURL().lastIndexOf("-")+1, (recipe.getRecipeURL().length()-1));
		recipe.setRecipeID(tempRecipeId);
		
		// Recipe Name
		WebElement recipeName = driver.findElement(By.xpath("//span[@id='ctl00_cntrightpanel_lblRecipeName']"));
		recipe.setRecipeName(recipeName.getText());

		// Recipe URL - already populated

		// //Recipe Category
		// WebElement recipeCategory = driver.findElement(By.xpath(""));

		// food category
		// WebElement foodCategory= driver.findElement(By.xpath(""));

		// ingredients
		List<WebElement> ingredients = driver.findElements(By.xpath("//*[@itemprop=\"recipeIngredient\"]"));
		StringBuffer ingredientsBuffer = new StringBuffer(256);
		for (WebElement ingredient : ingredients) {
			ingredientsBuffer.append(ingredient.getText());
			ingredientsBuffer.append("\n");
		}
		recipe.setIngredients(ingredientsBuffer.toString());

		// Preparation Time
		WebElement preparationTime = driver.findElement(By.xpath("//time[@itemprop='prepTime']"));
		recipe.setPreparationTime(preparationTime.getText());

		// cooking Time
		WebElement cookingTime = driver.findElement(By.xpath("//time[@itemprop='cookTime']"));
		recipe.setCookingTime(cookingTime.getText());

		// Preparation Method
//		WebElement preparationMethod = driver.findElement(By.xpath("//div[@id='recipe_small_steps']"));
//		recipe.setPreparationmethod(preparationMethod.getText());

		// Nutrient Values
//		WebElement nutrientValues = driver.findElement(By.xpath(""));

		// Targeted Morbid Conditions - Already populated
//		WebElement targettedMorbidConditions = driver
//				.findElement(By.xpath("//a[@id='ctl00_cntleftpanel_ttlhealthtree_tvTtlHealtht211']"));

	}

	/**
	 * @return
	 */
	private void populateRecipeLinks(ArrayList<String> links) {
		// list of WebElements that store all the links
		List<WebElement> recipeUrlsList = driver.findElements(By.className("rcc_recipename"));
		Iterator<WebElement> recipeUrlsListIter = recipeUrlsList.iterator();
		
		int counter = 0;
		// loop through recipeUrlsList to fill the links arraylist
		while ((recipeUrlsListIter.hasNext()) && (counter < MAX_RECEIPES_PER_MORBIDITY)) {
			WebElement link = (WebElement) recipeUrlsListIter.next();
			// .findElement -----> finds the tag <a> inside the current
						// WebElement
						// .getAttribute ----> returns the href attribute of the <a> tag in
						// the current WebElement
						links.add(link.findElement(By.tagName("a")).getAttribute("href"));
						counter ++;
		}
		
	}

	private void fetchAllRecipeUrls(ArrayList<String> allLInks, WebDriver driver) {
		// list of WebElements that store all the links
		WebElement pagination = driver.findElement(By.xpath("//div[@id='pagination']"));
		List<WebElement> links = pagination.findElements(By.tagName("a"));
		List<String> pageLinks = new ArrayList<String>();
		
		// loop through page links to fill the all links
		Iterator<WebElement> linksIter = links.iterator();
		int pageCounter = 0;
		while ((linksIter.hasNext()) && (pageCounter < MAX_PAGES_TO_FETCH)) {
			WebElement paginationLink = (WebElement) linksIter.next();
			String pageToLoad = paginationLink.getAttribute("href");
			pageLinks.add(pageToLoad);
			
			pageCounter ++;
			
		}


			
		

		for (String pageToLoad : pageLinks) {
			driver.get(pageToLoad);
			populateRecipeLinks(allLInks);
		}

	}

}