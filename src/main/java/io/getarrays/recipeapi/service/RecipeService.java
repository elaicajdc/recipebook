package io.getarrays.recipeapi.service;

import io.getarrays.recipeapi.domain.Recipe;
import io.getarrays.recipeapi.repo.RecipeRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.getarrays.recipeapi.constant.Constant.PHOTO_DIRECTORY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepo recipeRepo;

    public void deleteRecipe(String id) {
        recipeRepo.deleteById(id);
    }

    public Page<Recipe> getAllRecipes(int page, int size){
        return recipeRepo.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    public Recipe getRecipe(String id){
        return recipeRepo.findById(id).orElseThrow(() -> new RuntimeException("Recipe not found"));
    }

    public Recipe createRecipe(Recipe recipe) {
        return recipeRepo.save(recipe);
    }



    public String uploadPhoto(String id, MultipartFile file) {
        log.info("Saving picture for user ID: {}", id);
        Recipe recipe = getRecipe(id);
        String photoUrl = photoFunction.apply(id, file);
        recipe.setPhotoUrl(photoUrl);
        recipeRepo.save(recipe);
        return photoUrl;
    }

    //will give us the extension of the file. else, it will be .png
    private final Function<String, String> fileExtension = filename ->
            Optional.of(filename)
                    .filter(name -> name.contains("."))
                    .map(name -> "." + name.substring(name.lastIndexOf(".") + 1))
                    .orElse(".png");


    private final BiFunction<String, MultipartFile, String> photoFunction = (id, image) -> {
        String filename = id + fileExtension.apply(image.getOriginalFilename());
        try {
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
            if (filename.isEmpty()) {
                throw new RuntimeException("No file selected for upload");
            }
//            Recipe recipe = recipeRepo.findById(id).orElseThrow(() -> new RuntimeException("Recipe not found"));
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation); // create the directory if it doesn't exist
            }
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(id + fileExtension.apply(image.getOriginalFilename())), REPLACE_EXISTING);
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath().path("/recipes/image/" + id +
                            fileExtension.apply(image.getOriginalFilename())).toUriString();
        } catch (Exception exception) {
            throw new RuntimeException("Unable to save image", exception);
        }
    };



}
