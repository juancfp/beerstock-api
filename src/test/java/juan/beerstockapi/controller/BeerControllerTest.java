package juan.beerstockapi.controller;

import juan.beerstockapi.builder.BeerDTOBuilder;
import juan.beerstockapi.dto.BeerDTO;
import juan.beerstockapi.dto.QuantityDTO;
import juan.beerstockapi.exception.BeerNotFoundException;
import juan.beerstockapi.exception.BeerStockEmptyException;
import juan.beerstockapi.exception.BeerStockExceededException;
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

import java.util.Collections;

import static java.util.Collections.*;
import static juan.beerstockapi.utils.JsonConvertionUtils.asJsonString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.core.Is.is;

@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {

    //path da rest api
    private static final String BEER_API_URL_PATH = "/api/v1/beers";
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
        // com nomes certos -> retorna ok (feito)
        // com nome não registrado -> BeerNotFoundException (feito)
        // getList com beers -> retorna ok (feito)
        // getList sem beers -> retorna ok (feito)
    @Test
    void whenGETIsCalledWithValidNameThenOkStatusIsReturned() throws Exception{
        // given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //when (mock)
        when(beerService.findByName(beerDTO.getName())).thenReturn(beerDTO);

        // then
        // testar o status e o valor da requisição
        mockMvc.perform(get(BEER_API_URL_PATH+"/"+beerDTO.getName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(beerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
    }

    @Test
    void whenGETIsCalledWithoutRegisteredNameThenNotFoundStatusIsReturned() throws Exception {
        // given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //when
        when(beerService.findByName(beerDTO.getName())).thenThrow(BeerNotFoundException.class);

        //then
        // status veio de controller @RequestStatus(exception será enviada como status) e
        // definido em Exception
        mockMvc.perform(get(BEER_API_URL_PATH+"/"+beerDTO.getName())
                        .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(beerDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGETListWithBeersIsCalledThenOkStatusIsReturned() throws Exception {
        // given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //when
        when(beerService.listAll()).thenReturn(singletonList(beerDTO));

        // then
        mockMvc.perform(get(BEER_API_URL_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(beerDTO.getName())))
                .andExpect(jsonPath("$[0].brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$[0].type", is(beerDTO.getType().toString())));
    }

    @Test
    void whenGETListWithoutBeersIsCalledThenOkStatusIsReturned() throws Exception {
        // given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //when
        when(beerService.listAll()).thenReturn(singletonList(beerDTO));

        // then
        mockMvc.perform(get(BEER_API_URL_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // DELETE
        //DELETE com valid name -> retorno noContent()
        //DELETE com invalid name -> retorno notFound()

    @Test
    void whenDELETEIsCalledWithValidIdThenNoContentStatusIsReturned() throws Exception {
        // given
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //when
        doNothing().when(beerService).deleteById(beerDTO.getId());

        // then
        mockMvc.perform(delete(BEER_API_URL_PATH + "/" + beerDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenDELETEIsCalledWithInvalidIdThenNotFoundStatusIsReturned() throws Exception {
        //when
        doThrow(BeerNotFoundException.class).when(beerService).deleteById(INVALID_BEER_ID);

        // then
        mockMvc.perform(delete(BEER_API_URL_PATH + "/" + INVALID_BEER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // PATCH (quantityDTO)
        // incremento -> retorna ok (feito)
        // increment -> retorna bad request (feito)
        // increment -> retorna not found (feito)

        // decrement -> retorna ok
        // decrement -> retorna bad request
        // decrement -> retorna not found


        //increment
    @Test
    void whenPATCHIsCalledToIncrementDiscountThenOKstatusIsReturned() throws Exception {
        //given
        QuantityDTO quantityDTO = QuantityDTO.builder()
                .quantity(10)
                .build();

        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());

        //when
        when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity())).thenReturn(beerDTO);

        //then
        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(quantityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())))
                .andExpect(jsonPath("$.quantity", is(beerDTO.getQuantity())));
    }

    @Test
    void whenPATCHIsCalledToIncrementGreatherThanMaxThenBadRequestStatusIsReturned() throws Exception {

        //given
        QuantityDTO quantityDTO = QuantityDTO.builder()
                .quantity(30)
                .build();

        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());

        //when
        when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerStockExceededException.class);

        //then
        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(quantityDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPATCHIsCalledWithInvalidBeerIdToIncrementThenNotFoundStatusIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTO.builder()
                .quantity(30)
                .build();

        when(beerService.increment(INVALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerNotFoundException.class);
        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(quantityDTO)))
                .andExpect(status().isNotFound());
    }

        //decrement
    @Test
    void whenPATCHIsCalledToDecrementDiscountThenOKstatusIsReturned() throws Exception {
    //given
    QuantityDTO quantityDTO = QuantityDTO.builder()
            .quantity(5)
            .build();

    BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
    beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());

    //when
    when(beerService.decrement(VALID_BEER_ID, quantityDTO.getQuantity())).thenReturn(beerDTO);

    //then
    mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(quantityDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is(beerDTO.getName())))
            .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
            .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())))
            .andExpect(jsonPath("$.quantity", is(beerDTO.getQuantity())));
    }

    @Test
    void whenPATCHIsCalledToDecrementLowerThanZeroThenBadRequestStatusIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTO.builder()
                .quantity(20)
                .build();

        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTO.setQuantity(0);

        when(beerService.decrement(VALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerStockEmptyException.class);

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(quantityDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPATCHIsCalledWithInvalidBeerIdToDecrementThenNotFoundStatusIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTO.builder()
                .quantity(5)
                .build();

        when(beerService.decrement(INVALID_BEER_ID, quantityDTO.getQuantity())).thenThrow(BeerNotFoundException.class);
        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(quantityDTO)))
                .andExpect(status().isNotFound());
    }


}
