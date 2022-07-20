package juan.beerstockapi.controller;

import juan.beerstockapi.builder.BeerDTOBuilder;
import juan.beerstockapi.dto.BeerDTO;
import juan.beerstockapi.service.BeerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import static juan.beerstockapi.utils.JsonConvertionUtils.asJsonString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.core.Is.is;

@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {

    //path da rest api
    private static final String BEER_API_URL_PATH = "api/v1/beers";
    private static final long VALID_BEER_ID = 1L;
    private static final long INVALID_BEER_ID = 2l;
    private static final String BEER_API_SUBPATH_INCREMENT_URL = "/increment";
    private static final String BEER_API_SUBPATH_DECREMENT_URL = "/decrement";

    private MockMvc mockMvc;

    // cria mock
    @Mock
    private BeerService beerService;

    // injeta mock no controller
    @InjectMocks
    private BeerController beerController;

    //configura o mockmvc
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(beerController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
                .build();
    }

    // POST: criar com sucesso; criar com erro(falta argumento necessario);

    // POST: criar;
    @Test
    void whenPOSTIsCalledThenABeerIsCreated() throws Exception{
        // given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // when
        when(beerService.createBeer(beerDTO)).thenReturn(beerDTO);

        // then (testando)
        mockMvc.perform(post(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(beerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));

    }

    // POST com falta de argumentos
    @Test
    void whenPOSTIsCalledWithoutRequiredArgumentsThenAnErrorIsReturned() throws Exception{
        // given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTO.setBrand(null);

        // when
        // não tem necessidade, pois o próprio beerService retorna um erro

        // then
        mockMvc.perform(post(BEER_API_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(beerDTO)))
                .andExpect(status().isBadRequest());

    }

    // GET :
        // com nomes certos -> retorna ok
        // com nome não registrado -> BeerNotFoundException
        // getList com beers -> retorna ok
        // getList sem beers -> retorna ok

//    @Test
//    void whenGETIsCalledWithAValidNameThenAOkStatusIsReturned() throws Exception{
//        // given
//        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
//
//        // when
//        when(beerService.findByName(beerDTO.getName())).thenReturn(beerDTO);
//    }







}