package juan.beerstockapi.service;

import juan.beerstockapi.exception.BeerStockEmptyException;
import lombok.AllArgsConstructor;
import juan.beerstockapi.dto.BeerDTO;
import juan.beerstockapi.entity.Beer;
import juan.beerstockapi.exception.BeerAlreadyRegisteredException;
import juan.beerstockapi.exception.BeerNotFoundException;
import juan.beerstockapi.exception.BeerStockExceededException;
import juan.beerstockapi.mapper.BeerMapper;
import juan.beerstockapi.repository.BeerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper = BeerMapper.INSTANCE;

    public BeerDTO createBeer(BeerDTO beerDTO) throws BeerAlreadyRegisteredException {
        verifyIfIsAlreadyRegistered(beerDTO.getName());
        Beer beer = beerMapper.toModel(beerDTO);
        Beer savedBeer = beerRepository.save(beer);
        return beerMapper.toDTO(savedBeer);
    }

    public BeerDTO findByName(String name) throws BeerNotFoundException {
        Beer foundBeer = beerRepository.findByName(name)
                .orElseThrow(() -> new BeerNotFoundException(name));
        return beerMapper.toDTO(foundBeer);
    }

    public List<BeerDTO> listAll() {
        return beerRepository.findAll()
                .stream()
                .map(beerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) throws BeerNotFoundException {
        verifyIfExists(id);
        beerRepository.deleteById(id);
    }

    private void verifyIfIsAlreadyRegistered(String name) throws BeerAlreadyRegisteredException {
        Optional<Beer> optSavedBeer = beerRepository.findByName(name);
        if (optSavedBeer.isPresent()) {
            throw new BeerAlreadyRegisteredException(name);
        }
    }

    private Beer verifyIfExists(Long id) throws BeerNotFoundException {
        return beerRepository.findById(id)
                .orElseThrow(() -> new BeerNotFoundException(id));
    }

    public BeerDTO increment(Long id, int quantityToIncrement) throws BeerNotFoundException, BeerStockExceededException {
        Beer beerToIncrementStock = verifyIfExists(id);
        int quantityAfterIncrement = quantityToIncrement + beerToIncrementStock.getQuantity();
        if (quantityAfterIncrement <= beerToIncrementStock.getMax()) {
            beerToIncrementStock.setQuantity(quantityAfterIncrement);
            Beer incrementedBeerStock = beerRepository.save(beerToIncrementStock);
            return beerMapper.toDTO(incrementedBeerStock);
        }
        throw new BeerStockExceededException(id, quantityToIncrement);
    }

    public BeerDTO decrement(Long id, int quantityToDecrement) throws BeerNotFoundException, BeerStockEmptyException {
        Beer beerToDecrementStock = verifyIfExists(id);
        int quantityAfterDecrement = beerToDecrementStock.getQuantity() - quantityToDecrement;
        if (quantityAfterDecrement >= 0) {
            beerToDecrementStock.setQuantity(quantityAfterDecrement);
            Beer incrementedBeerStock = beerRepository.save(beerToDecrementStock);
            return beerMapper.toDTO(incrementedBeerStock);
        }
        throw new BeerStockEmptyException(id, quantityToDecrement);
    }
}
