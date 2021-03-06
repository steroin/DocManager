package pl.docmanager.dao.category;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import pl.docmanager.dao.exception.EntityValidationException;
import pl.docmanager.domain.category.Category;
import pl.docmanager.domain.category.CategoryBuilder;
import pl.docmanager.domain.category.CategoryItem;
import pl.docmanager.domain.category.CategoryItemBuilder;
import pl.docmanager.domain.category.CategoryItemState;
import pl.docmanager.domain.page.PageBuilder;
import pl.docmanager.domain.solution.Solution;
import pl.docmanager.domain.solution.SolutionBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static pl.docmanager.domain.category.CategoryItemContentType.CATEGORY;
import static pl.docmanager.domain.category.CategoryItemContentType.PAGE;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryItemDaoTest {

    @Autowired
    private CategoryItemDao categoryItemDao;

    @MockBean
    private CategoryItemRepository categoryItemRepository;
    @MockBean
    private CategoryRepository categoryRepository;

    private CategoryItem categoryItem1;
    private CategoryItem categoryItem2;
    private CategoryItem categoryItem3;
    private CategoryItem categoryItem4;
    private CategoryItem categoryItem5;
    private CategoryItem categoryItem6;

    @Before
    public void setup() {
        Solution solution1 = new SolutionBuilder(1).build();
        Category category1 = new CategoryBuilder(1, solution1).withName("exampleCategory").build();
        categoryItem1 = new CategoryItemBuilder(1, category1).build();
        categoryItem2 = new CategoryItemBuilder(2, category1).build();
        categoryItem3 = new CategoryItemBuilder(3, category1).build();

        given(categoryRepository.findById(1L)).willReturn(Optional.of(category1));
        given(categoryItemRepository.findAllByCategory_Id(1))
                .willReturn(Arrays.asList(categoryItem1, categoryItem2, categoryItem3));

        Solution solution2 = new SolutionBuilder(2).build();
        Category category2 = new CategoryBuilder(2, solution2).withName("exampleCategory").build();
        categoryItem4 = new CategoryItemBuilder(4, category2).build();
        categoryItem5 = new CategoryItemBuilder(5, category2).build();
        categoryItem6 = new CategoryItemBuilder(6, category2).build();

        given(categoryRepository.findById(2L)).willReturn(Optional.of(category2));
        given(categoryItemRepository.findAllByCategory_Id(2))
                .willReturn(Arrays.asList(categoryItem4, categoryItem5, categoryItem6));

        Category category3 = new CategoryBuilder(3, solution1).withName("anotherExampleCategory").build();

        given(categoryRepository.findById(3L)).willReturn(Optional.of(category3));
        given(categoryItemRepository.findAllByCategory_Id(3)).willReturn(new ArrayList<>());
    }

    @Test
    public void getAllByCategoryIdTestValid() {
        List<CategoryItem> categoryItems = Arrays.asList(categoryItem1, categoryItem2, categoryItem3);
        assertTrue(categoryItems.containsAll(categoryItemDao.getAllByCategoryId(1)));
    }

    @Test
    public void getAllByCategoryIdTestValidCategoryEmpty() {
        assertTrue(categoryItemDao.getAllByCategoryId(3).isEmpty());
    }

    @Test
    public void getAllByCategoryIdTestNonExistingCategory() {
        assertTrue(categoryItemDao.getAllByCategoryId(5).isEmpty());
    }

    @Test
    public void addAllTestValid() {
        CategoryItem categoryItemValid1 = new CategoryItemBuilder(1, new Category())
                .withContentType(PAGE)
                .withContentPage(new PageBuilder(1, new Solution()).build())
                .withIndex(0)
                .withState(CategoryItemState.ACTIVE)
                .build();
        CategoryItem categoryItemValid2 = new CategoryItemBuilder(1, new Category())
                .withContentType(CATEGORY)
                .withContentCategory(new CategoryBuilder(1, new Solution()).build())
                .withIndex(1)
                .withState(CategoryItemState.ACTIVE)
                .build();
        CategoryItem categoryItemValid3 = new CategoryItemBuilder(1, new Category())
                .withContentType(PAGE)
                .withContentPage(new PageBuilder(1, new Solution()).build())
                .withIndex(2)
                .withState(CategoryItemState.ACTIVE)
                .build();
        List<CategoryItem> toAdd = Arrays.asList(categoryItemValid1, categoryItemValid2, categoryItemValid3);
        categoryItemDao.addAll(toAdd);
        verify(categoryItemRepository, times(1)).saveAll(toAdd);
    }

    @Test(expected = EntityValidationException.class)
    public void addAllTestOneInvalid() {
        CategoryItem categoryItemValid1 = new CategoryItemBuilder(1, new Category())
                .withContentType(PAGE)
                .withContentPage(new PageBuilder(1, new Solution()).build())
                .withIndex(0)
                .withState(CategoryItemState.ACTIVE)
                .build();
        CategoryItem categoryItemValid2 = new CategoryItemBuilder(1, new Category())
                .withContentType(CATEGORY)
                .withContentCategory(new CategoryBuilder(1, new Solution()).build())
                .withIndex(1)
                .withState(CategoryItemState.ACTIVE)
                .build();
        CategoryItem categoryItemInvalid = new CategoryItemBuilder(1, null)
                .withContentType(PAGE)
                .withContentPage(new PageBuilder(1, new Solution()).build())
                .withIndex(2)
                .withState(CategoryItemState.ACTIVE)
                .build();
        List<CategoryItem> toAdd = Arrays.asList(categoryItemValid1, categoryItemValid2, categoryItemInvalid);
        categoryItemDao.addAll(toAdd);
    }
}