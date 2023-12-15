package com.numpy.scraping.team17.model;

public class Recipe {

	private String recipeID;
	private String recipeName;
	private String recipeURL;
	private String recipeCategory;
	private String foodCategory;
	private String ingredients;
	private String preparationTime;
	private String cookingTime;
	private String preparationMethod;
	private String nutrientValues;
	private String targettedMorbidConditions;

	public String getRecipeCategory() {
		return recipeCategory;
	}

	public void setRecipeCategory(String recipeCategory) {
		this.recipeCategory = recipeCategory;
	}

	public String getFoodCategory() {
		return foodCategory;
	}

	public void setFoodCategory(String foodCategory) {
		this.foodCategory = foodCategory;
	}

	public String getIngredients() {
		return ingredients;
	}

	public void setIngredients(String ingredients) {
		this.ingredients = ingredients;
	}

	public String getPreparationTime() {
		return preparationTime;
	}

	public void setPreparationTime(String preparationTime) {
		this.preparationTime = preparationTime;
	}

	public String getCookingTime() {
		return cookingTime;
	}

	public void setCookingTime(String cookingTime) {
		this.cookingTime = cookingTime;
	}

	public String getPreparationmethod() {
		return preparationMethod;
	}

	public void setPreparationmethod(String preparationmethod) {
		this.preparationMethod = preparationmethod;
	}

	public String getNutrientValues() {
		return nutrientValues;
	}

	public void setNutrientValues(String nutrientValues) {
		this.nutrientValues = nutrientValues;
	}

	public String getTargettedMorbidConditions() {
		return targettedMorbidConditions;
	}

	public void setTargettedMorbidConditions(String targettedMorbidConditions) {
		this.targettedMorbidConditions = targettedMorbidConditions;
	}

	public String getRecipeID() {
		return recipeID;
	}

	public void setRecipeID(String recipeID) {
		this.recipeID = recipeID;
	}

	public String getRecipeName() {
		return recipeName;
	}

	public void setRecipeName(String recipeName) {
		this.recipeName = recipeName;
	}

	public String getRecipeURL() {
		return recipeURL;
	}

	public void setRecipeURL(String recipeURL) {
		this.recipeURL = recipeURL;
	}

	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer();

		strBuf.append(" RecipeId : " + recipeID);
		strBuf.append("\n");
		strBuf.append(" Recipe Name : " + recipeName);
		strBuf.append("\n");
		strBuf.append(" Recipe URL : " + recipeURL);
		strBuf.append("\n");
		strBuf.append(" Food Category : " + foodCategory);
		strBuf.append("\n");
		strBuf.append(" Recipe Category : " + recipeCategory);
		strBuf.append("\n");
		strBuf.append(" Preparation Time : " + preparationTime);
		strBuf.append("\n");
		strBuf.append(" Cooking Time : " + cookingTime);
		strBuf.append("\n");
		strBuf.append(" Preparation Method : " + preparationMethod);
		strBuf.append("\n");
		strBuf.append(" Ingredients : " + ingredients);
		strBuf.append("\n");
		strBuf.append(" Nutrient Values : " + nutrientValues);
		strBuf.append("\n");
		strBuf.append(" Targetted Morbid Conditions : " + targettedMorbidConditions);
		strBuf.append("\n");

		return strBuf.toString();

	}

}
