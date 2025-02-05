package io.getarrays.recipeapi.resource;

import io.getarrays.recipeapi.domain.Recipe;
import io.getarrays.recipeapi.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.getarrays.recipeapi.constant.Constant.PHOTO_DIRECTORY;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@RestController
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeResource {
    private final RecipeService recipeService;

    @DeleteMapping("/{id}")
    public String deleteRecipe(@PathVariable(value = "id") String id) {
        recipeService.deleteRecipe(id);
        return "Recipe deleted successfully.";
    }

    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        return ResponseEntity.created(URI.create("/recipes/" + recipe.getId())).body(recipeService.createRecipe(recipe));
    }

    @GetMapping
    public ResponseEntity<Page<Recipe>> getRecipe(@RequestParam(value = "page", defaultValue = "0") int page,
                                                     @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok().body(recipeService.getAllRecipes(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipe(@PathVariable(value = "id") String id) {
        return ResponseEntity.ok().body(recipeService.getRecipe(id));
    }

    @PutMapping("/image")
    public ResponseEntity<String> uploadPhoto(@RequestParam("id") String id, @RequestParam("file")MultipartFile file) {
        return ResponseEntity.ok().body(recipeService.uploadPhoto(id, file));
    }

    @GetMapping(path = "/image/{filename}", produces = { IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE })
    public byte[] getPhoto(@PathVariable("filename") String filename) throws IOException {
        return Files.readAllBytes(Paths.get(PHOTO_DIRECTORY, filename));
    }


}