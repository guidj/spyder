package org.verg.spyder.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.verg.spyder.domain.Image;
import org.verg.spyder.repositories.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class ImageService {
	
	@Autowired
	private ImageRepository imageRepository;
	
	public long getNumberImages() {
		return this.imageRepository.count();
	}
	
	public void save(Image image){
		this.imageRepository.save(image);
	}
	
	public Image createImage(String url, boolean downloaded) {
		return this.imageRepository.save(new Image(url, downloaded));
	}
	
	public Iterable<Image> getAllImages() {
		return this.imageRepository.findAll();
	}
	
	public Image findImageById(Long id) {
		return this.imageRepository.findOne(id);
	}

    // This is using the schema based index
	public Image findImageByUrl(String url) {
		return this.imageRepository.findBySchemaPropertyValue("url", url);
	}
	
	public Iterable<Image> getUnprocessedImages(){
		return this.imageRepository.getImages(false, 10);
	}
}
